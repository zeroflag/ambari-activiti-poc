  package com.example.workflow.servicetask;

  import java.util.ArrayList;
  import java.util.Arrays;

  import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
  import org.apache.ambari.server.AsyncServiceTask;

  public class InstallAdditionalNamenode extends AsyncServiceTask {
    public void execute(ActivityExecution context) {
      String hostName = hosts(context).newNameNodeHost;
      LOG.info("Install Additional Namenode to " + hostName + " activitId:" + context.getId());
      Long id = api.installComponent(hostName, "NAMENODE");
      pendingTasks.add(context.getProcessInstance().getId(), context.getId(), new ArrayList<>(Arrays.asList(id)));
    }
  }
