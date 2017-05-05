package com.example.workflow.servicetask;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;
import org.apache.ambari.server.RoleCommand;

public class StartAdditionalNamenode extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Starting Additional Namenode activitId:" + context.getId());
    api.registerCommand(context.getId(), api.sendCommandToComponent("HDFS", "NAMENODE", hosts(context).newNameNodeHost, RoleCommand.START));
  }
}
