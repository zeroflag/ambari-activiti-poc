package com.example.workflow.servicetask;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.ambari.server.AsyncServiceTask;

public class InstallJournalNodes extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    LOG.info("Installing Journal Node activitId:" + context.getId());
    List<Long> ids = hosts(context).journalNodeHosts.stream()
      .map(each -> api.installComponent(each, "JOURNALNODE"))
      .collect(toList());
    pendingTasks.add(context.getId(), ids);
  }
}
