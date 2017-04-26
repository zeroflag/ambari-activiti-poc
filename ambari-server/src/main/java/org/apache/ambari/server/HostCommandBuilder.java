package org.apache.ambari.server;

import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.ServiceComponentHostEvent;
import org.apache.ambari.server.state.svccomphost.ServiceComponentHostOpInProgressEvent;

public class HostCommandBuilder {
  private String host;
  private String service;
  private Role role;
  private RoleCommand command;
  private Cluster cluster;

  public HostCommandBuilder hostName(String host) {
    this.host = host;
    return this;
  }

  public HostCommandBuilder service(String service) {
    this.service = service;
    return this;
  }

  public HostCommandBuilder role(Role role) {
    this.role = role;
    return this;
  }

  public HostCommandBuilder command(RoleCommand command) {
    this.command = command;
    return this;
  }

  public HostCommandBuilder cluster(Cluster cluster) {
    this.cluster = cluster;
    return this;
  }

  public HostCommand build() {
    return new HostCommand(
      host,
      service,
      role,
      command,
      cluster,
      event(role, host)
    );
  }

  private ServiceComponentHostEvent event(Role role, String host) {
    return new ServiceComponentHostOpInProgressEvent(role.toString(), host, System.currentTimeMillis());
  }
}
