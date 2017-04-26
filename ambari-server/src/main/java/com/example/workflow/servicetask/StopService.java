package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.apache.ambari.server.RoleCommand;

public class StopService extends ServerTask {
  private Expression serviceName;

  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Stopping " + serviceName.getExpressionText());
    api().sendCommandToService(serviceName.getExpressionText(), RoleCommand.STOP);
  }
}
