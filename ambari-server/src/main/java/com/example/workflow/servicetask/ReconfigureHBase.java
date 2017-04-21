package com.example.workflow.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ReconfigureHBase implements JavaDelegate {
  public void execute(DelegateExecution context) throws Exception {
    System.out.println("Reconfiguring HBase");
  }
}
