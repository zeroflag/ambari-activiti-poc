package com.example.workflow.servicetask;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.ambari.groovy.client.AmbariClient;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.HostCommandBuilder;
import org.apache.ambari.server.Role;
import org.apache.ambari.server.RoleCommand;
import org.apache.ambari.server.WorkflowApi;

import com.example.ui.Hosts;
import com.google.gson.Gson;

import groovyx.net.http.HttpResponseException;

public abstract class ServerTask implements JavaDelegate {
  private static final Gson gson = new Gson();
  protected final AmbariClient client = new AmbariClient("c6401.ambari.apache.org");

  protected void waitForRequest(int requestId) throws InterruptedException {
    int count = 0;
    long progress = client.getRequestProgress(requestId).longValue();
    while (progress < 100 ) {
      System.out.print(".");
      if (++count % 20 == 0)
        System.out.print(progress + "%");
      if (progress < 0)
        throw new RuntimeException("Request failed: " + requestId);
      Thread.sleep(1000);
      progress = client.getRequestProgress(requestId).longValue();
    }
    System.out.println(".");
  }

  protected void startComponentBlocking(String hostName, String component) throws HttpResponseException, InterruptedException {
    waitForRequest(client.startComponentsOnHost(hostName, singletonList(component)).get(component));
  }

  protected void installComponentBlocking(String hostName, String component) throws HttpResponseException, InterruptedException {
    int requestId = client.installComponentsToHost(hostName, singletonList(component)).get(component);
    waitForRequest(requestId);
  }

  protected Hosts hosts(DelegateExecution context) {
    Map<String, Object> hosts = gson.fromJson((String)context.getVariable("additionalNameNodeHost"), Map.class);
    return new Hosts((String)hosts.get("currentNameNodeHost"), (String)hosts.get("newNameNodeHost"), (List<String>) hosts.get("journalNodeHosts"));
  }

  protected String serviceId(DelegateExecution context) {
    return ((String) context.getVariable("nameServiceId"));
  }

  protected WorkflowApi api() {
    return WorkflowApi.getInstance();
  }

  public void startService(String serviceName) {
    try {
      api().sendCommandToService(serviceName, RoleCommand.START);
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }
  }

  public void stopService(String serviceName) {
    try {
      api().sendCommandToService(serviceName, RoleCommand.STOP);
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }
  }

  public void startComponent(String serviceName, String componentName, String host) {
    try {
      api().sendCommandToComponent(serviceName, componentName, host, RoleCommand.START);
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }
  }

  public void stopComponent(String serviceName, String componentName, String host) {
    try {
      api().sendCommandToComponent(serviceName, componentName, host, RoleCommand.STOP);
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }

  }

//  public void installComponent(String serviceName, String componentName, List<String> hosts) {
//    try {
//      for (String host : hosts) {
//        api().sendInstallCommand(serviceName, componentName, host);
//      }
//    } catch (AmbariException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  public void deleteComponent(String serviceName, String componentName, List<String> hosts) {
//    try {
//      for (String host : hosts) {
//        api().sendUninstallCommand(serviceName, componentName, host);
//      }
//    } catch (AmbariException e) {
//      throw new RuntimeException(e);
//    }
//  }

//  public void startAll() {
//    try {
//      api().modifyAll("START");
//    } catch (AmbariException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  public void stopAll() {
//    try {
//      api().modifyAll("STOP");
//    } catch (AmbariException e) {
//      throw new RuntimeException(e);
//    }
//  }
}
