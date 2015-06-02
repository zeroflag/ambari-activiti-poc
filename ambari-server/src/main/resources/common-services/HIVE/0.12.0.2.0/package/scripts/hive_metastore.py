#!/usr/bin/env python
"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""
from resource_management.core.logger import Logger
from resource_management.core.resources.system import Execute
from resource_management.libraries.script import Script
from resource_management.libraries.functions import conf_select
from resource_management.libraries.functions import hdp_select
from resource_management.libraries.functions.format import format
from resource_management.libraries.functions.version import format_hdp_stack_version
from resource_management.libraries.functions.version import compare_versions
from resource_management.libraries.functions.security_commons import build_expectations
from resource_management.libraries.functions.security_commons import cached_kinit_executor
from resource_management.libraries.functions.security_commons import get_params_from_filesystem
from resource_management.libraries.functions.security_commons import validate_security_config_properties
from resource_management.libraries.functions.security_commons import FILE_TYPE_XML

from hive import hive
from hive_service import hive_service
from ambari_commons.os_family_impl import OsFamilyImpl
from ambari_commons import OSConst
from atlas_plugin_utils import configure_for_plugin

# the legacy conf.server location in HDP 2.2
LEGACY_HIVE_SERVER_CONF = "/etc/hive/conf.server"

class HiveMetastore(Script):
  def install(self, env):
    import params
    self.install_packages(env, exclude_packages = params.hive_exclude_packages)

  def start(self, env, rolling_restart=False):
    import params
    env.set_params(params)
    self.configure(env)  # FOR SECURITY
    hive_service('metastore', action='start', rolling_restart=rolling_restart)

  def stop(self, env, rolling_restart=False):
    import params
    env.set_params(params)
    hive_service('metastore', action='stop')

  def configure(self, env):
    import params
    env.set_params(params)
    savedConfig = configure_for_plugin(self.command_data_file)
    self.install_packages(env, exclude_packages = params.hive_exclude_packages)
    Script.config = savedConfig
    hive(name = 'metastore')


@OsFamilyImpl(os_family=OSConst.WINSRV_FAMILY)
class HiveMetastoreWindows(HiveMetastore):
  def status(self, env):
    import status_params
    from resource_management.libraries.functions import check_windows_service_status
    check_windows_service_status(status_params.hive_metastore_win_service_name)


@OsFamilyImpl(os_family=OsFamilyImpl.DEFAULT)
class HiveMetastoreDefault(HiveMetastore):
  def get_stack_to_component(self):
    return {"HDP": "hive-metastore"}

  def status(self, env):
    import status_params
    from resource_management.libraries.functions import check_process_status

    env.set_params(status_params)
    pid_file = format("{hive_pid_dir}/{hive_metastore_pid}")
    # Recursively check all existing gmetad pid files
    check_process_status(pid_file)

  def pre_rolling_restart(self, env):
    Logger.info("Executing Metastore Rolling Upgrade pre-restart")
    import params
    env.set_params(params)

    if Script.is_hdp_stack_greater_or_equal("2.3"):
      self.upgrade_schema(env)

    if params.version and compare_versions(format_hdp_stack_version(params.version), '2.2.0.0') >= 0:
      conf_select.select(params.stack_name, "hive", params.version)
      hdp_select.select("hive-metastore", params.version)

  def security_status(self, env):
    import status_params
    env.set_params(status_params)
    if status_params.security_enabled:
      props_value_check = {"hive.server2.authentication": "KERBEROS",
                           "hive.metastore.sasl.enabled": "true",
                           "hive.security.authorization.enabled": "true"}
      props_empty_check = ["hive.metastore.kerberos.keytab.file",
                           "hive.metastore.kerberos.principal"]

      props_read_check = ["hive.metastore.kerberos.keytab.file"]
      hive_site_props = build_expectations('hive-site', props_value_check, props_empty_check,
                                            props_read_check)

      hive_expectations ={}
      hive_expectations.update(hive_site_props)

      security_params = get_params_from_filesystem(status_params.hive_conf_dir,
                                                   {'hive-site.xml': FILE_TYPE_XML})
      result_issues = validate_security_config_properties(security_params, hive_expectations)
      if not result_issues: # If all validations passed successfully
        try:
          # Double check the dict before calling execute
          if 'hive-site' not in security_params \
            or 'hive.metastore.kerberos.keytab.file' not in security_params['hive-site'] \
            or 'hive.metastore.kerberos.principal' not in security_params['hive-site']:
            self.put_structured_out({"securityState": "UNSECURED"})
            self.put_structured_out({"securityIssuesFound": "Keytab file or principal are not set property."})
            return

          cached_kinit_executor(status_params.kinit_path_local,
                                status_params.hive_user,
                                security_params['hive-site']['hive.metastore.kerberos.keytab.file'],
                                security_params['hive-site']['hive.metastore.kerberos.principal'],
                                status_params.hostname,
                                status_params.tmp_dir)

          self.put_structured_out({"securityState": "SECURED_KERBEROS"})
        except Exception as e:
          self.put_structured_out({"securityState": "ERROR"})
          self.put_structured_out({"securityStateErrorInfo": str(e)})
      else:
        issues = []
        for cf in result_issues:
          issues.append("Configuration file %s did not pass the validation. Reason: %s" % (cf, result_issues[cf]))
        self.put_structured_out({"securityIssuesFound": ". ".join(issues)})
        self.put_structured_out({"securityState": "UNSECURED"})
    else:
      self.put_structured_out({"securityState": "UNSECURED"})

  def upgrade_schema(self, env):
    """
    Executes the schema upgrade binary.  This is its own function because it could
    be called as a standalone task from the upgrade pack, but is safe to run it for each
    metastore instance.
    """
    Logger.info("Upgrading Hive Metastore")
    import params
    env.set_params(params)

    if params.security_enabled:
      kinit_command=format("{kinit_path_local} -kt {smoke_user_keytab} {smokeuser_principal}; ")
      Execute(kinit_command,user=params.smokeuser)

    binary = format("/usr/hdp/{version}/hive/bin/schematool")

    # the conf.server directory changed locations between HDP 2.2 and 2.3
    # since the configurations have not been written out yet during an upgrade
    # we need to choose the original legacy location
    schematool_hive_server_conf_dir = params.hive_server_conf_dir
    if params.current_version is not None:
      current_version = format_hdp_stack_version(params.current_version)
      if compare_versions(current_version, "2.3") < 0:
        schematool_hive_server_conf_dir = LEGACY_HIVE_SERVER_CONF

    env_dict = {
      'HIVE_CONF_DIR': schematool_hive_server_conf_dir
    }

    command = format("{binary} -dbType {hive_metastore_db_type} -upgradeSchema")
    Execute(command, user=params.hive_user, tries=1, environment=env_dict, logoutput=True)

if __name__ == "__main__":
  HiveMetastore().execute()
