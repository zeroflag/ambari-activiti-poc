package org.apache.ambari.server;

import java.util.Map;

public interface ServiceTaskApi {
  Long sendCommandToComponent(String service, String component, String host, RoleCommand command);
  Long sendCommandToService(String service, RoleCommand command);

  Long installComponent(String hostName, String component);
  void uninstallComponent(String service, String component, String hostName);

  void modifyConfig(Map config);

  Long startAllServices();
  Long stopAllServices();
}
