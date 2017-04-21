package com.example.workflow.servicetask;

import static java.util.Collections.singletonList;

import org.activiti.engine.delegate.DelegateExecution;

public class DeleteSecondaryNamenode extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Deleting Secondary NameNode");
    client.deleteHostComponents(hosts(context).currentNameNodeHost, singletonList("SECONDARY_NAMENODE"));
  }
}
