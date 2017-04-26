package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.ambari.server.RoleCommand;

public class StartAdditionalNamenode extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting Additional Namenode");
    api().sendCommandToComponent("HDFS", "NAMENODE", hosts(context).newNameNodeHost, RoleCommand.START);
  }
}
