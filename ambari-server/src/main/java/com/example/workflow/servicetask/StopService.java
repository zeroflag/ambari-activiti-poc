package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;
import org.apache.ambari.server.RoleCommand;

public class StopService extends AsyncServiceTask {
  private Expression serviceName;

  public void execute(ActivityExecution context) {
    LOG.info("Stopping " + serviceName.getExpressionText() + " activitId:" + context.getId());
    Long id = api.sendCommandToService(serviceName.getExpressionText(), RoleCommand.STOP);
    pendingTasks.add(context.getProcessInstance().getId(), context.getId(), Arrays.asList(id));
  }
}
