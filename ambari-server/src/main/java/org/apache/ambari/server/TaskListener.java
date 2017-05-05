package org.apache.ambari.server;

public interface TaskListener {
  void taskCompleted(String activityId);
}
