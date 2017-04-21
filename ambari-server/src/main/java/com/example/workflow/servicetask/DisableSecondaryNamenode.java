package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;

public class DisableSecondaryNamenode extends ServerTask {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Disabling Secondary Namenode");
//    TODO
//    PUT http://c6401.ambari.apache.org:8080/api/v1/clusters/cc/hosts/c6401.ambari.apache.org/host_components/SECONDARY_NAMENODE
//    {"RequestInfo":{},"Body":{"HostRoles":{"maintenance_state":"ON"}}}:
  }
}
