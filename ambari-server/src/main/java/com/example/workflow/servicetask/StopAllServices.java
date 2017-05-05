package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class StopAllServices extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Stopping all services activitId:" + context.getId());
    Long id = api.stopAllServices();
    pendingTasks.add(context.getId(), Arrays.asList(id));
  }
}
