package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class StartAllServices extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting All services");
    waitForRequest(client.startAllServices());
  }
}
