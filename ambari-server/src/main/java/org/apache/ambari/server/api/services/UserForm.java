package org.apache.ambari.server.api.services;

public class UserForm {
  private final String id;
  private final String name;

  public UserForm(String id, String name) {
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
