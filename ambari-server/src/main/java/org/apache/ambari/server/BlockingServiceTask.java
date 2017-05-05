package org.apache.ambari.server;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.example.ui.Hosts;
import com.google.gson.Gson;
import com.google.inject.Inject;

@StaticallyInject
public abstract class BlockingServiceTask implements JavaDelegate {
  private static final Gson gson = new Gson();
  protected static @Inject ServiceTaskApi api;

  protected Hosts hosts(DelegateExecution context) {
    Map<String, Object> hosts = gson.fromJson((String)context.getVariable("additionalNameNodeHost"), Map.class);
    return new Hosts((String)hosts.get("currentNameNodeHost"), (String)hosts.get("newNameNodeHost"), (List<String>) hosts.get("journalNodeHosts"));
  }

  protected String serviceId(DelegateExecution context) {
    return ((String) context.getVariable("nameServiceId"));
  }
}
