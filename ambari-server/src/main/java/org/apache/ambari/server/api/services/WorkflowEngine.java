package org.apache.ambari.server.api.services;

import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.task.Task;

public interface WorkflowEngine {
  String startProcess();

  List<UserTask> getTasks();

  boolean processEnded(String processId);

  void completeUserTask(String taskId, Map<String, Object> variables);

  List<UserForm> formData(String taskId);
}
