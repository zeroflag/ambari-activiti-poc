package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;
import org.apache.ambari.server.RoleCommand;

public class StartFailoverController extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Starting failover controller activitId:" + context.getId());
    Long id1 = api.sendCommandToComponent("HDFS", "ZKFC", hosts(context).currentNameNodeHost, RoleCommand.START);
    Long id2 = api.sendCommandToComponent("HDFS", "ZKFC", hosts(context).newNameNodeHost, RoleCommand.START);
    api.registerCommand(context.getId(), Arrays.asList(id1, id2));
  }
}
