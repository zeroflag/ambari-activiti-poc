package com.example.workflow.servicetask;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class StartAllServices extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Starting All services activitId:" + context.getId());
    api.registerCommand(context.getId(), api.startAllServices());
  }
}
