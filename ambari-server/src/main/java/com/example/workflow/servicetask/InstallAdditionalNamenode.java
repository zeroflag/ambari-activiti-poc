  package com.example.workflow.servicetask;

  import org.activiti.engine.delegate.DelegateExecution;

  public class InstallAdditionalNamenode extends ServerTask {
    public void execute(DelegateExecution context) throws Exception {
      String hostName = hosts(context).newNameNodeHost;
      System.out.println("Install Additional Namenode to " + hostName);
      installComponentBlocking(hostName, "NAMENODE");
    }
  }
