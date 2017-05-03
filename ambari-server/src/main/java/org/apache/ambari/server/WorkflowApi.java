package org.apache.ambari.server;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.RuntimeService;
import org.apache.ambari.server.actionmanager.ActionManager;
import org.apache.ambari.server.actionmanager.RequestFactory;
import org.apache.ambari.server.actionmanager.StageFactory;
import org.apache.ambari.server.api.services.PersistKeyValueImpl;
import org.apache.ambari.server.controller.AmbariCustomCommandExecutionHelper;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.internal.ClusterResourceProvider;
import org.apache.ambari.server.controller.internal.HostComponentResourceProvider;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.utilities.ClusterControllerHelper;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.orm.dao.RequestDAO;
import org.apache.ambari.server.orm.entities.RequestEntity;
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
  private Map<String,List<Long>> pendingCommands = new ConcurrentHashMap<>();
  private volatile boolean stopped = false;

  public Long sendCommandToComponent(String service, String component, String host, RoleCommand command) {
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

  public Long sendCommandToService(String service, RoleCommand command) {
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

  public Long sendHostCommands(String requestContext, HostCommand... hostCommands) {
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

  public WorkflowApi() {
    INSTANCE = this;
  }

  public static WorkflowApi getInstance() {
    return INSTANCE;
  }

  public void init(RuntimeService runtimeService) {
    actionManager = injector.getInstance(ActionManager.class);
    requestFactory = injector.getInstance(RequestFactory.class);
    stageFactory = injector.getInstance(StageFactory.class);
    clusters = injector.getInstance(Clusters.class);
    roleGraphFactory = injector.getInstance(RoleGraphFactory.class);
    ambariManagementController = injector.getInstance(AmbariManagementController.class);
    customCommandExecutionHelper = injector.getInstance(AmbariCustomCommandExecutionHelper.class);
    requestDAO = injector.getInstance(RequestDAO.class);
    LOG.info("STARTING pending task thread");
    Thread thread = new Thread() {
      @Override
      public void run() {
        while (!stopped) {
          try {
            checkPendingCommands(runtimeService);
          } catch (RuntimeException e) {
            LOG.error("Error", e);
          }
        }
      }
    };
    thread.start();
  }

  public Long installComponent(String hostName, String component) {
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

  public void uninstallComponent(String service, String component, String hostName) {
    setAuthentication();
    sendCommandToComponent(service, component, hostName, RoleCommand.STOP);
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

  public void modifyComponent(Map config) {
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

  public Long startAll() {
    try {
      return sendHostCommands("Start ALL", serverComponents(RoleCommand.START));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Long stopAll() {
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

  private void checkPendingCommands(RuntimeService runtimeService) {
    Iterator<Map.Entry<String, List<Long>>> iter = pendingCommands.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, List<Long>> entry = iter.next();
      String activityId = entry.getKey();
      List<Long> requestIds = entry.getValue();
      for (Iterator<Long> iterator = requestIds.iterator(); iterator.hasNext(); ) {
        Long requestId = iterator.next();
        if (requestId == null) {
          LOG.info("Command completed: {} activitiId: {}", requestId, activityId);
          iterator.remove();
        } else {
          RequestEntity requestEntity = requestDAO.findByPks(Arrays.asList(requestId), true).get(0);
          if (requestEntity.getStatus().isCompletedState()) {
            LOG.info("Command completed: {} activitiId: {}", requestId, activityId);
            iterator.remove();
          }
        }
      }
      if (requestIds.isEmpty()) {
        LOG.info("Notifying activity: "+ activityId);
        iter.remove();                     // HTC:1
        runtimeService.signal(activityId); // HTC:2
      }
    }
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  public synchronized void registerCommand(String activitiId, Long requestId) {
    registerCommand(activitiId, new ArrayList<>(Arrays.asList(requestId)));
  }

  public synchronized void registerCommand(String activitiId, List<Long> requestIds) {
    pendingCommands.put(activitiId, new ArrayList<>(requestIds));
  }
}
