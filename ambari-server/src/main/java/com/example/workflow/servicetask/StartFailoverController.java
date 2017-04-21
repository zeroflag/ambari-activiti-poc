package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class StartFailoverController extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Starting failover controller");
    startComponentBlocking(hosts(context).currentNameNodeHost, "ZKFC");
    startComponentBlocking(hosts(context).newNameNodeHost, "ZKFC");
  }
}
