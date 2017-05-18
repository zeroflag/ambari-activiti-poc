package org.apache.ambari.server.api.services;

import static org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.ambari.server.TaskListener;

public class ActivitiWorkflowEngine implements WorkflowEngine, TaskListener {
  private final ProcessEngine processEngine;
  private final RuntimeService runtimeService;
  private final TaskService taskService;
  private final FormService formService;
  private final HistoryService historyService;

  public ActivitiWorkflowEngine() {
    this.processEngine = processEngine();
    this.taskService = processEngine.getTaskService();
    this.formService = processEngine.getFormService();
    this.runtimeService = processEngine.getRuntimeService();
    this.historyService = processEngine.getHistoryService();
  }

  private ProcessEngine processEngine() {
    return new StandaloneProcessEngineConfiguration()
      .setAsyncExecutorEnabled(true)
      .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
      .setJdbcUsername("sa")
      .setJdbcPassword("")
      .setJdbcDriver("org.h2.Driver")
      .setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE)
      .buildProcessEngine();
  }

  @Override
  public String startProcess() {
    deploy("enable-namenode-ha.bpmn");
    ProcessInstance process = runtimeService.startProcessInstanceByKey("enableNamenodeHaProcess");
    return process.getId();
  }

  @Override
  public void stopProcess(String processExecutionId) {
    runtimeService.deleteProcessInstance(processExecutionId, "stop");
  }

  private void deploy(String fileName) {
    processEngine.getRepositoryService()
      .createDeployment()
      .addClasspathResource(fileName)
      .deploy();
  }

  @Override
  public List<UserTask> getTasks(String processExecutionId) {
    return taskService.createTaskQuery().executionId(processExecutionId).list()
      .stream()
      .map(task -> new UserTask(task.getId(), task.getName(), task.getFormKey(), requiredInputs(formData(task.getId()))))
      .collect(Collectors.toList());
  }

  private Map<String, Object> requiredInputs(List<UserForm> userForms) {
    Map<String, Object> inputs = new HashMap<>();
    for (UserForm form : userForms) {
      inputs.put(form.getId(), null);
    }
    return inputs;
  }

  @Override
  public boolean processEnded(String processId) {
    return reloadProcess(processId).map(proc ->proc.isEnded()).orElse(true);
  }

  @Override
  public void completeUserTask(String taskId, Map<String, Object> variables) {
    taskService.complete(taskId, variables);
  }

  @Override
  public List<UserForm> formData(String taskId) {
    return formService.getTaskFormData(taskId).getFormProperties()
      .stream()
      .map(form -> new UserForm(form.getId(), form.getName()))
      .collect(Collectors.toList());
  }

  private Optional<ProcessInstance> reloadProcess(String processId) {
    return Optional.ofNullable(runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(processId)
      .singleResult());
  }

  @Override
  public void taskCompleted(String activityId) {
    runtimeService.signal(activityId);
  }
}
