package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.ambari.server.RoleCommand;

public class DeleteSecondaryNamenode extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Deleting Secondary NameNode");
    api().sendCommandToComponent("HDFS", "SECONDARY_NAMENODE", hosts(context).currentNameNodeHost, RoleCommand.UNINSTALL);
  }
}
