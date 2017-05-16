package org.apache.ambari.server.api.services;

import java.util.List;
import java.util.Map;

public interface WorkflowEngine {
  String startProcess();

  void stopProcess(String processId);

  List<UserTask> getTasks(String executionId);

  boolean processEnded(String processId);

  void completeUserTask(String taskId, Map<String, Object> variables);

  List<UserForm> formData(String taskId);

}
