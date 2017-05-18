package org.apache.ambari.server.api.services;

import java.util.Map;

public class UserTask {
  private final String id;
  private final String title;
  private final String viewId;
  private final Map<String, Object> requiredInput;

  public UserTask(String id, String title, String viewId, Map<String, Object> requiredInput) {
    this.id = id;
    this.title = title;
    this.viewId = viewId;
    this.requiredInput = requiredInput;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Map<String, Object> getRequiredInput() {
    return requiredInput;
  }

  public String getViewId() {
    return viewId;
  }
}
