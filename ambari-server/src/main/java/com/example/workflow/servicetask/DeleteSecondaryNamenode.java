package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.ambari.server.BlockingServiceTask;

public class DeleteSecondaryNamenode extends BlockingServiceTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Deleting Secondary NameNode activitId:" + context.getId() + " from " + hosts(context).currentNameNodeHost);
    api.uninstallComponent("HDFS", "SECONDARY_NAMENODE", hosts(context).currentNameNodeHost);
  }
}
