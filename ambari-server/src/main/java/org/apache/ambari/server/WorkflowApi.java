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
import org.apache.ambari.server.controller.internal.HostComponentResourceProvider;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.utilities.ClusterControllerHelper;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.orm.dao.RequestDAO;
import org.apache.ambari.server.orm.entities.RequestEntity;
import org.apache.ambari.server.stageplanner.RoleGraphFactory;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.ServiceComponent;
import org.apache.ambari.server.state.ServiceComponentHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

@EagerSingleton
public class WorkflowApi {
  private static Logger LOG = LoggerFactory.getLogger(WorkflowApi.class);
  private static WorkflowApi INSTANCE;
  @Inject
  private Injector injector; // XXX direct injection of fields doesn't work
  private ActionManager actionManager;
  private RequestFactory requestFactory;
  private StageFactory stageFactory;
  private Clusters clusters;
  private RoleGraphFactory roleGraphFactory;
  private AmbariManagementController ambariManagementController;
  private AmbariCustomCommandExecutionHelper customCommandExecutionHelper;
  private RequestDAO requestDAO;

  public void sendCommandToComponent(String service, String component, String host, RoleCommand command) {
    sendHostCommands(
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

  public void sendCommandToService(String service, RoleCommand command) {
    try {
      sendHostCommands(command + " " + service, components(service, command));
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

  public void sendHostCommands(String requestContext, HostCommand... hostCommands) {
    try {
      waitForCompletion(new StageContainerBuilder(actionManager, requestFactory, roleGraphFactory, ambariManagementController)
        .stage(
          new StageBuilder(stageFactory, customCommandExecutionHelper)
            .requestContext(requestContext)
            .cluster(cluster())
            .hostCommands(hostCommands))
        .persisted());
    } catch (AmbariException e) {
      throw new RuntimeException(e);
    }
  }

  private Cluster cluster() {
    return clusters.getClusters().values().iterator().next();
  }

  public WorkflowApi() {
    INSTANCE = this;
  }

  public static WorkflowApi getInstance() {
    INSTANCE.init();
    return INSTANCE;
  }

  private void waitForCompletion(Long requestId) {
    RequestEntity requestEntity = requestDAO.findByPks(Arrays.asList(requestId), true).get(0);
    while (!requestEntity.getStatus().isCompletedState()) {
      System.out.println("Waiting for command: " + requestEntity.getStatus());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      requestEntity = requestDAO.findByPks(Arrays.asList(requestId), true).get(0);
    }
  }

  private void init() {
    actionManager = injector.getInstance(ActionManager.class);
    requestFactory = injector.getInstance(RequestFactory.class);
    stageFactory = injector.getInstance(StageFactory.class);
    clusters = injector.getInstance(Clusters.class);
    roleGraphFactory = injector.getInstance(RoleGraphFactory.class);
    ambariManagementController = injector.getInstance(AmbariManagementController.class);
    customCommandExecutionHelper = injector.getInstance(AmbariCustomCommandExecutionHelper.class);
    requestDAO = injector.getInstance(RequestDAO.class);
  }

  public void installComponent(String hostName, String service, String component) {
    HostComponentResourceProvider hostComponentResourceProvider = (HostComponentResourceProvider) ClusterControllerHelper.getClusterController().ensureResourceProvider(Resource.Type.HostComponent);
    Map<String, Object> properties = new HashMap<>();
    properties.put("HostRoles/component_name", component);
    properties.put("HostRoles/cluster_name", cluster().getClusterName());
    properties.put("HostRoles/host_name", hostName);
    try {
      hostComponentResourceProvider.createResources(PropertyHelper.getCreateRequest(Collections.singleton(properties), null));
      RequestStatusResponse response = hostComponentResourceProvider.install(cluster().getClusterName(), hostName, Collections.emptyList(), Arrays.asList(component), true);
      waitForCompletion(response.getRequestId());
      sendCommandToComponent(service, component, hostName, RoleCommand.START);
    } catch (ResourceAlreadyExistsException e) {
      LOG.info("Component {} already exists on host {}", component, hostName);
      return;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void uninstallComponent(String hostName, String service, String component) {
    HostComponentResourceProvider hostComponentResourceProvider = (HostComponentResourceProvider) ClusterControllerHelper.getClusterController().ensureResourceProvider(Resource.Type.HostComponent);
    Map<String, Object> properties = new HashMap<>();
    properties.put("HostRoles/component_name", component);
    properties.put("HostRoles/cluster_name", cluster().getClusterName());
    properties.put("HostRoles/host_name", hostName);
    try {
      hostComponentResourceProvider.deleteResources(PropertyHelper.getCreateRequest(Collections.singleton(properties), null), null);
      // TODO block
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
