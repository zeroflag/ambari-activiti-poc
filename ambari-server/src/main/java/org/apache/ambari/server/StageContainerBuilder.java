package org.apache.ambari.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.ambari.server.actionmanager.ActionManager;
import org.apache.ambari.server.actionmanager.RequestFactory;
import org.apache.ambari.server.actionmanager.Stage;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.internal.RequestStageContainer;
import org.apache.ambari.server.stageplanner.RoleGraph;
import org.apache.ambari.server.stageplanner.RoleGraphFactory;
import org.apache.ambari.server.state.Cluster;

public class StageContainerBuilder {
  private final RoleGraphFactory roleGraphFactory;
  private final AmbariManagementController ambariManagementController;
  private final List<Stage> stages = new ArrayList<>();
  private final RequestStageContainer requestStageContainer;

  public StageContainerBuilder(ActionManager actionManager, RequestFactory requestFactory, RoleGraphFactory roleGraphFactory, AmbariManagementController ambariManagementController) {
    this.roleGraphFactory = roleGraphFactory;
    this.ambariManagementController = ambariManagementController;
    this.requestStageContainer = new RequestStageContainer(actionManager.getNextRequestId(), null, requestFactory, actionManager);
  }

  public StageContainerBuilder stage(StageBuilder stageBuilder) throws AmbariException {
    Stage stage = stageBuilder
      .requestId(requestStageContainer.getId())
      .stageId(requestStageContainer.getLastStageId())
      .build();
    stages.addAll(roleGraph(stageBuilder.cluster(), stage).getStages());
    return this;
  }

  private RoleGraph roleGraph(Cluster cluster, Stage stage) {
    RoleGraph roleGraph = roleGraphFactory.createNew(ambariManagementController.getRoleCommandOrder(cluster));
    roleGraph.build(stage);
    return roleGraph;
  }

  public Long persisted() throws AmbariException {
    RequestStageContainer container = this.build();
    container.persist();
    return container.getId();
  }

  public RequestStageContainer build() {
    requestStageContainer.addStages(stages);
    return requestStageContainer;
  }
}
