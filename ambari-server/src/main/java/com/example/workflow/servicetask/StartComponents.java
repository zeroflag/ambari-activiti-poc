package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.ambari.server.RoleCommand;

public class StartComponents extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting components");
    api().sendCommandToService("ZOOKEEPER", RoleCommand.START);
    api().sendCommandToComponent("HDFS", "NAMENODE", hosts(context).currentNameNodeHost, RoleCommand.START);
  }
}
