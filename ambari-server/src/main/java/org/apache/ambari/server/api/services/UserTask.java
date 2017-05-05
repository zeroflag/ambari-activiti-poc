package org.apache.ambari.server.api.services;

class UserTask {
  private final String id;
  private final String name;

  public UserTask(String id, String name) {
    this.id = id;
    this.name = name;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
}
