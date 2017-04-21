package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class StartComponents extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting components");
    waitForRequest(client.startService("ZOOKEEPER"));
    startComponentBlocking(hosts(context).currentNameNodeHost, "NAMENODE");
  }
}
