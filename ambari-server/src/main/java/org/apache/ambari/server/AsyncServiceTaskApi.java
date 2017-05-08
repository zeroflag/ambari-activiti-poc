package org.apache.ambari.server;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ambari.server.actionmanager.ActionManager;
import org.apache.ambari.server.actionmanager.RequestFactory;
import org.apache.ambari.server.actionmanager.StageFactory;
import org.apache.ambari.server.controller.AmbariCustomCommandExecutionHelper;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.internal.ClusterResourceProvider;
import org.apache.ambari.server.controller.internal.HostComponentResourceProvider;
import org.apache.ambari.server.controller.spi.ClusterController;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.utilities.ClusterControllerHelper;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.security.authorization.internal.InternalAuthenticationToken;
import org.apache.ambari.server.stageplanner.RoleGraphFactory;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.Host;
import org.apache.ambari.server.state.ServiceComponent;
import org.apache.ambari.server.state.ServiceComponentHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class AsyncServiceTaskApi implements ServiceTaskApi {
  private static Logger LOG = LoggerFactory.getLogger(AsyncServiceTaskApi.class);
  private ActionManager actionManager;
  private RequestFactory requestFactory;
  private StageFactory stageFactory;
  private Clusters clusters;
  private RoleGraphFactory roleGraphFactory;
  private AmbariManagementController ambariManagementController;
  private AmbariCustomCommandExecutionHelper customCommandExecutionHelper;
  private Injector injector;

  // XXX direct injection of fields doesn't work
  @Inject
  public AsyncServiceTaskApi(Injector injector) {
    this.injector = injector;
  }

  private void init() {
    actionManager = injector.getInstance(ActionManager.class);
    requestFactory = injector.getInstance(RequestFactory.class);
    stageFactory = injector.getInstance(StageFactory.class);
    clusters = injector.getInstance(Clusters.class);
    roleGraphFactory = injector.getInstance(RoleGraphFactory.class);
    ambariManagementController = injector.getInstance(AmbariManagementController.class);
    customCommandExecutionHelper = injector.getInstance(AmbariCustomCommandExecutionHelper.class);
  }

  @Override
  public Long sendCommandToComponent(String service, String component, String host, RoleCommand command) {
    init();
    return sendHostCommands(
      command + " " + component,
      new HostCommandBuilder()
        .hostName(host)
        .service(service)
        .role(Role.valueOf(component))
        .command(command)
        .cluster(cluster())
        .build()
    );
  }

  @Override
  public Long sendCommandToService(String service, RoleCommand command) {
    init();
    try {
      return sendHostCommands(command + " " + service, components(service, command));
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }
  }

  private HostCommand[] components(String service, RoleCommand command) throws AmbariException {
    List<HostCommand> hostCommands = new ArrayList<>();
    for (ServiceComponent component : cluster().getService(service).getServiceComponents().values()) {
      for (ServiceComponentHost host : component.getServiceComponentHosts().values()) {
        hostCommands.add(
          new HostCommandBuilder()
            .hostName(host.getHostName())
            .service(component.getServiceName())
            .role(Role.valueOf(component.getName()))
            .command(command)
            .cluster(cluster())
            .build()

        );
      }
    }
    return hostCommands.toArray(new HostCommand[0]);
  }

  private Long sendHostCommands(String requestContext, HostCommand... hostCommands) {
    init();
    try {
      return new StageContainerBuilder(actionManager, requestFactory, roleGraphFactory, ambariManagementController)
        .stage(
          new StageBuilder(stageFactory, customCommandExecutionHelper)
            .requestContext(requestContext)
            .cluster(cluster())
            .hostCommands(hostCommands))
        .persisted();
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }
  }

  private Cluster cluster() {
    return clusters.getClusters().values().iterator().next();
  }

  @Override
  public Long installComponent(String hostName, String component) {
    init();
    setAuthentication();
    HostComponentResourceProvider hostComponentResourceProvider = (HostComponentResourceProvider) ClusterControllerHelper.getClusterController().ensureResourceProvider(Resource.Type.HostComponent);
    Map<String, Object> properties = new HashMap<>();
    properties.put("HostRoles/component_name", component);
    properties.put("HostRoles/cluster_name", cluster().getClusterName());
    properties.put("HostRoles/host_name", hostName);
    try {
      hostComponentResourceProvider.createResources(PropertyHelper.getCreateRequest(Collections.singleton(properties), null));
      RequestStatusResponse response = hostComponentResourceProvider.install(cluster().getClusterName(), hostName, Collections.emptyList(), Arrays.asList(component), true);
      return response.getRequestId();
    } catch (ResourceAlreadyExistsException e) {
      LOG.info("Component {} already exists on host {}", component, hostName);
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void uninstallComponent(String service, String component, String hostName) {
    init();
    setAuthentication();
    HostComponentResourceProvider hostComponentResourceProvider = (HostComponentResourceProvider) ClusterControllerHelper.getClusterController().ensureResourceProvider(Resource.Type.HostComponent);
    Map<String, Object> properties = new HashMap<>();
    try {
      hostComponentResourceProvider.deleteResources(PropertyHelper.getCreateRequest(Collections.singleton(properties), null),
        new PredicateBuilder()
        .begin()
        .property("HostRoles/cluster_name").equals(cluster().getClusterName())
        .and()
        .property("HostRoles/host_name").equals(hostName)
        .and()
        .property("HostRoles/component_name").equals(component)
        .end().toPredicate());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void modifyConfig(Map config) {
    init();
    setAuthentication();
    ClusterResourceProvider configResourceProvider = (ClusterResourceProvider) ClusterControllerHelper.getClusterController().ensureResourceProvider(Resource.Type.Cluster);
    try {
      configResourceProvider.updateResources(PropertyHelper.getCreateRequest(Collections.singleton(config), null),
        new PredicateBuilder()
          .begin()
          .property("Clusters/cluster_name").equals(cluster().getClusterName())
          .end().toPredicate());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setAuthentication() {
    InternalAuthenticationToken authentication = new InternalAuthenticationToken("server_action_executor");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Override
  public Long startAllServices() {
    init();
    Map<String, Object> properties = new HashMap<>();
    properties.put("ServiceInfo/state", "STARTED");
    properties.put("ServiceInfo/cluster_name", cluster().getClusterName());
    try {
      ClusterControllerHelper.getClusterController().updateResources(
        Resource.Type.Service,
        PropertyHelper.getCreateRequest(Collections.singleton(properties), null),
        new PredicateBuilder()
          .begin()
          .property("ServiceInfo/cluster_name").equals(cluster().getClusterName())
          .end()
          .toPredicate()
      );
      return null; // TODO: id
//      return sendHostCommands("Start ALL", serverComponents(RoleCommand.START)); doesn't work
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Long stopAllServices() {
    init();
    try {
      return sendHostCommands("Stop ALL", serverComponents(RoleCommand.STOP));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HostCommand[] serverComponents(RoleCommand command) throws AmbariException {
    List<HostCommand> hostCommands = new ArrayList<>();
    for (Host host : clusters.getHosts()) {
      for (ServiceComponentHost service : cluster().getServiceComponentHosts(host.getHostName())) {
        if (service.isClientComponent()) continue;
        hostCommands.add(
          new HostCommandBuilder()
            .hostName(host.getHostName())
            .service(service.getServiceName())
            .role(Role.valueOf(service.getServiceComponentName()))
            .command(command)
            .cluster(cluster())
            .build());
      }
    }
    return hostCommands.toArray(new HostCommand[0]);
  }
}
