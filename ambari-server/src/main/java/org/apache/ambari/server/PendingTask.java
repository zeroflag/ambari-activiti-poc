package org.apache.ambari.server;

import java.util.List;

public class PendingTask {
  public final String processExecutionId;
  public final String activitiId;
  public final List<Long> requestId;

  public PendingTask(String processExecutionId, String activitiId, List<Long> requestId) {
    this.processExecutionId = processExecutionId;
    this.activitiId = activitiId;
    this.requestId = requestId;
  }
}
