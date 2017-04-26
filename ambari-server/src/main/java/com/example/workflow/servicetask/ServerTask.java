package com.example.workflow.servicetask;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.ambari.groovy.client.AmbariClient;
import org.apache.ambari.server.WorkflowApi;

import com.example.ui.Hosts;
import com.google.gson.Gson;

public abstract class ServerTask implements JavaDelegate {
  private static final Gson gson = new Gson();
  // TODO delme
  protected final AmbariClient client = new AmbariClient("c6401.ambari.apache.org");

  // TODO delme
  protected void waitForRequest(int requestId) throws InterruptedException {
    int count = 0;
    long progress = client.getRequestProgress(requestId).longValue();
    while (progress < 100 ) {
      System.out.print(".");
      if (++count % 20 == 0)
        System.out.print(progress + "%");
      if (progress < 0)
        throw new RuntimeException("Request failed: " + requestId);
      Thread.sleep(1000);
      progress = client.getRequestProgress(requestId).longValue();
    }
    System.out.println(".");
  }

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
