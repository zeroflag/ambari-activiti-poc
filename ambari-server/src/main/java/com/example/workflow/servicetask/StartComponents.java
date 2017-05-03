package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.RoleCommand;

public class StartComponents extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Starting components activitId:" + context.getId());
    Long id1 = api().sendCommandToService("ZOOKEEPER", RoleCommand.START);
    Long id2 = api().sendCommandToComponent("HDFS", "NAMENODE", hosts(context).currentNameNodeHost, RoleCommand.START);
    api().registerCommand(context.getId(), Arrays.asList(id1, id2));
  }
}
