  package com.example.workflow.servicetask;

  import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
  import org.apache.ambari.server.AsyncServiceTask;

  public class InstallAdditionalNamenode extends AsyncServiceTask {
    public void execute(ActivityExecution context) {
      String hostName = hosts(context).newNameNodeHost;
      System.out.println("Install Additional Namenode to " + hostName + " activitId:" + context.getId());
      api.registerCommand(context.getId(), api.installComponent(hostName, "NAMENODE"));
    }
  }
