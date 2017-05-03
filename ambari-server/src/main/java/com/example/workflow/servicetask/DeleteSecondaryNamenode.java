package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class DeleteSecondaryNamenode extends BlockingServiceTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Deleting Secondary NameNode activitId:" + context.getId());
    api().uninstallComponent("HDFS", "SECONDARY_NAMENODE", hosts(context).currentNameNodeHost);
  }
}
