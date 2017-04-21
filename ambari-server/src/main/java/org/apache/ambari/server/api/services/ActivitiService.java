package org.apache.ambari.server.api.services;

import static org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public class ActivitiService {
  private final ProcessEngine processEngine;
  private final RuntimeService runtimeService;
  private final TaskService taskService;
  private final FormService formService;

  public ActivitiService() {
    this.processEngine = processEngine();
    this.taskService = processEngine.getTaskService();
    this.formService = processEngine.getFormService();
    this.runtimeService = processEngine.getRuntimeService();
  }

  private static ProcessEngine processEngine() {
    return new StandaloneProcessEngineConfiguration()
      .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
      .setJdbcUsername("sa")
      .setJdbcPassword("")
      .setJdbcDriver("org.h2.Driver")
      .setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE).buildProcessEngine();
  }

  public ProcessInstance startProcess() {
    deploy("enable-namenode-ha.bpmn");
    return runtimeService.startProcessInstanceByKey("enableNamenodeHaProcess");
  }

  private void deploy(String fileName) {
    processEngine.getRepositoryService()
      .createDeployment()
      .addClasspathResource(fileName)
      .deploy();
  }

  public List<Task> getTasks() {
    return taskService.createTaskQuery().list();
  }

  public boolean processEnded(String processId) {
    return reloadProcess(processId).map(proc ->proc.isEnded()).orElse(true);
  }

  public void completeUserTask(String taskId, Map<String, Object> variables) {
    taskService.complete(taskId, variables);
  }

  public List<FormProperty> formData(String taskId) {
    return formService.getTaskFormData(taskId).getFormProperties();
  }

  private Optional<ProcessInstance> reloadProcess(String processId) {
    return Optional.ofNullable(runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(processId)
      .singleResult());
  }
}
