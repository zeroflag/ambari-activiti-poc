package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.ambari.server.RoleCommand;

public class StartJournalNodes extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting journal node");
    for (String each : hosts(context).journalNodeHosts)
      api().sendCommandToComponent("HDFS", "JOURNALNODE", each, RoleCommand.START);
  }
}
