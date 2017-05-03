package com.example.workflow.servicetask;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.RoleCommand;

public class StartJournalNodes extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Starting journal node activitId:" + context.getId());
    List<Long> ids = new ArrayList<>();
    for (String each : hosts(context).journalNodeHosts) {
      Long id = api().sendCommandToComponent("HDFS", "JOURNALNODE", each, RoleCommand.START);
      ids.add(id);
    }
    api().registerCommand(context.getId(), ids);
  }
}
