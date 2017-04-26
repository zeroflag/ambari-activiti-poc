package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class InstallFailoverController extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Install failover controller");
    api().installComponent(hosts(context).currentNameNodeHost, "ZKFC");
    api().installComponent(hosts(context).newNameNodeHost, "ZKFC");
  }
}
