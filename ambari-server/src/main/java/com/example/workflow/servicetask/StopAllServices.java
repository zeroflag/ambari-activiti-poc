package com.example.workflow.servicetask;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class StopAllServices extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Stopping all services activitId:" + context.getId());
    api.registerCommand(context.getId(), api.stopAllServices());
  }
}
