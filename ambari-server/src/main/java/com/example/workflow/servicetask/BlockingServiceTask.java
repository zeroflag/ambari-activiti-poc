package com.example.workflow.servicetask;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.ambari.server.WorkflowApi;

import com.example.ui.Hosts;
import com.google.gson.Gson;

public abstract class BlockingServiceTask implements JavaDelegate {
  private static final Gson gson = new Gson();

  protected Hosts hosts(DelegateExecution context) {
    Map<String, Object> hosts = gson.fromJson((String)context.getVariable("additionalNameNodeHost"), Map.class);
    return new Hosts((String)hosts.get("currentNameNodeHost"), (String)hosts.get("newNameNodeHost"), (List<String>) hosts.get("journalNodeHosts"));
  }

  protected String serviceId(DelegateExecution context) {
    return ((String) context.getVariable("nameServiceId"));
  }

  protected WorkflowApi api() {
    return WorkflowApi.getInstance();
  }
}
