package com.example.workflow.servicetask;

import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class InstallFailoverController extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Install failover controller activitId:" + context.getId());
    Long id1 = api().installComponent(hosts(context).currentNameNodeHost, "ZKFC");
    Long id2 = api().installComponent(hosts(context).newNameNodeHost, "ZKFC");
    api().registerCommand(context.getId(), Arrays.asList(id1, id2));
  }
}
