package org.apache.ambari.server;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

import com.example.ui.Hosts;
import com.google.gson.Gson;
import com.google.inject.Inject;

@StaticallyInject
public abstract class AsyncServiceTask extends TaskActivityBehavior {
  private static final Gson gson = new Gson();
  protected static @Inject ServiceTaskApi api;

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

  abstract public void execute(ActivityExecution execution);

  protected Hosts hosts(ActivityExecution context) {
    Map<String, Object> hosts = gson.fromJson((String)context.getVariable("additionalNameNodeHost"), Map.class);
    return new Hosts((String)hosts.get("currentNameNodeHost"), (String)hosts.get("newNameNodeHost"), (List<String>) hosts.get("journalNodeHosts"));
  }

  protected String serviceId(DelegateExecution context) {
    return ((String) context.getVariable("nameServiceId"));
  }
}
