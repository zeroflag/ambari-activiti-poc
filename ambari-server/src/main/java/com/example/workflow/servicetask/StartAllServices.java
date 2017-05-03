package com.example.workflow.servicetask;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class StartAllServices extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Starting All services activitId:" + context.getId());
    api().registerCommand(context.getId(), api().startAll());
  }
}
