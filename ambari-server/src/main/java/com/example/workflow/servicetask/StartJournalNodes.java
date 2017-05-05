package com.example.workflow.servicetask;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;
import org.apache.ambari.server.RoleCommand;

public class StartJournalNodes extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Starting journal node activitId:" + context.getId());
    List<Long> ids = hosts(context).journalNodeHosts.stream()
      .map(each -> api.sendCommandToComponent("HDFS", "JOURNALNODE", each, RoleCommand.START))
      .collect(toList());
    api.registerCommand(context.getId(), ids);
  }
}
