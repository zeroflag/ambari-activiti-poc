package org.apache.ambari.server.api.services;

import java.util.Map;

public class UserTaskCompletion {
  private final String status;
  private final Map<String,Object> required;

  public UserTaskCompletion(String status, Map<String, Object> required) {
    this.status = status;
    this.required = required;
  }

  public String getStatus() {
    return status;
  }

  public Map<String, Object> getRequired() {
    return required;
  }
}
