package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class StartAllServices extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Starting All services activitId:" + context.getId());
    Long id = api.startAllServices();
    pendingTasks.add(context.getId(), Arrays.asList(id));
  }
}
