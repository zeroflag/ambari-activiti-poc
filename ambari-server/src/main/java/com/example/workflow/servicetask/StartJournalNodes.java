package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class StartJournalNodes extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting journal node");
    for (String each : hosts(context).journalNodeHosts)
      startComponentBlocking(each, "JOURNALNODE");
  }
}
