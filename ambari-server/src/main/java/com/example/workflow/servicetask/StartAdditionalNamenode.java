package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;
import org.apache.ambari.server.RoleCommand;

public class StartAdditionalNamenode extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Starting Additional Namenode activitId:" + context.getId());
    Long id = api.sendCommandToComponent("HDFS", "NAMENODE", hosts(context).newNameNodeHost, RoleCommand.START);
    pendingTasks.registerCommand(context.getId(), Arrays.asList(id));
  }
}
