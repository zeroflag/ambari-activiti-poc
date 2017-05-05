package org.apache.ambari.server;

import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;

public interface ServiceTaskApi {
  Long sendCommandToComponent(String service, String component, String host, RoleCommand command);

  Long sendCommandToService(String service, RoleCommand command);

  Long sendHostCommands(String requestContext, HostCommand... hostCommands);

  Long installComponent(String hostName, String component);

  void uninstallComponent(String service, String component, String hostName);

  void modifyConfig(Map config);

  Long startAllServices();

  Long stopAllServices();

  void registerCommand(String activitiId, List<Long> requestIds);
}
