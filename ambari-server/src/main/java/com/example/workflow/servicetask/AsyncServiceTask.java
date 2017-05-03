package com.example.workflow.servicetask;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.WorkflowApi;

import com.example.ui.Hosts;
import com.google.gson.Gson;

public abstract class AsyncServiceTask extends TaskActivityBehavior {
  private static final Gson gson = new Gson();

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

  abstract public void execute(ActivityExecution execution);

  protected Hosts hosts(ActivityExecution context) {
    Map<String, Object> hosts = gson.fromJson((String)context.getVariable("additionalNameNodeHost"), Map.class);
    return new Hosts((String)hosts.get("currentNameNodeHost"), (String)hosts.get("newNameNodeHost"), (List<String>) hosts.get("journalNodeHosts"));
  }

  protected String serviceId(DelegateExecution context) {
    return ((String) context.getVariable("nameServiceId"));
  }

  protected WorkflowApi api() { return WorkflowApi.getInstance(); }
}
