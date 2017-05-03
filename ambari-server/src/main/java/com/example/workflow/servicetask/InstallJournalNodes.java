package com.example.workflow.servicetask;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class InstallJournalNodes extends AsyncServiceTask {
  public void execute(ActivityExecution context) {
    System.out.println("Installing Journal Node activitId:" + context.getId());
    List<Long> ids = new ArrayList<>();
    for (String each : hosts(context).journalNodeHosts) {
      Long id = api().installComponent(each, "JOURNALNODE");
      ids.add(id);
    }
    api().registerCommand(context.getId(), ids);
  }
}
