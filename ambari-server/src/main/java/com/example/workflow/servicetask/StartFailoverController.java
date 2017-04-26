package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.ambari.server.RoleCommand;

public class StartFailoverController extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting failover controller");
    api().sendCommandToComponent("HDFS", "ZKFC", hosts(context).currentNameNodeHost, RoleCommand.START);
    api().sendCommandToComponent("HDFS", "ZKFC", hosts(context).newNameNodeHost, RoleCommand.START);
  }
}
