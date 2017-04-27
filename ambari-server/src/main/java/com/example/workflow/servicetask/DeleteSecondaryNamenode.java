package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class DeleteSecondaryNamenode extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Deleting Secondary NameNode");
    api().uninstallComponent(hosts(context).currentNameNodeHost, "HDFS", "SECONDARY_NAMENODE");
  }
}
