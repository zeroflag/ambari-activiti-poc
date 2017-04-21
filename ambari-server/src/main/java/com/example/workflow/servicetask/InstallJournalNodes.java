package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class InstallJournalNodes extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Installing Journal Node");
    for (String each : hosts(context).journalNodeHosts)
      installComponentBlocking(each, "JOURNALNODE");
  }
}
