package org.apache.ambari.server;

import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.ServiceComponentHostEvent;

public class HostCommand {
  public final String host;
  public final String service;
  public final Role role;
  public final RoleCommand command;
  public final Cluster cluster;
  public final ServiceComponentHostEvent event;

  public HostCommand(String host, String service, Role role, RoleCommand command, Cluster cluster, ServiceComponentHostEvent event) {
    this.host = host;
    this.service = service;
    this.role = role;
    this.command = command;
    this.cluster = cluster;
    this.event = event;
  }
}
