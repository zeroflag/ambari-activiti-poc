package com.example.workflow.servicetask;

import java.util.ArrayList;
import java.util.Arrays;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class InstallFailoverController extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Install failover controller activitId:" + context.getId());
    Long id1 = api.installComponent(hosts(context).currentNameNodeHost, "ZKFC");
    Long id2 = api.installComponent(hosts(context).newNameNodeHost, "ZKFC");
    pendingTasks.add(context.getProcessInstance().getId(), context.getId(), new ArrayList<>(Arrays.asList(id1, id2)));
  }
}
