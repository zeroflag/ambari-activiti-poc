package org.apache.ambari.server;

import static java.util.Collections.singletonList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.ambari.server.actionmanager.Stage;
import org.apache.ambari.server.actionmanager.StageFactory;
import org.apache.ambari.server.controller.ActionExecutionContext;
import org.apache.ambari.server.controller.AmbariCustomCommandExecutionHelper;
import org.apache.ambari.server.controller.internal.RequestResourceFilter;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.utils.StageUtils;

public class StageBuilder {
  private final StageFactory stageFactory;
  private final AmbariCustomCommandExecutionHelper customCommandExecutionHelper;
  private long requestId;
  private long stageId;
  private String requestContext;
  private Cluster cluster;
  private final List<HostCommand> hostCommands = new ArrayList<>();

  public StageBuilder(StageFactory stageFactory, AmbariCustomCommandExecutionHelper customCommandExecutionHelper) {
    this.stageFactory = stageFactory;
    this.customCommandExecutionHelper = customCommandExecutionHelper;
  }

  public StageBuilder stageId(long stageId) {
    this.stageId = stageId;
    return this;
  }

  public StageBuilder requestId(long requestId) {
    this.requestId = requestId;
    return this;
  }

  public StageBuilder requestContext(String requestContext) {
    this.requestContext = requestContext;
    return this;
  }

  public StageBuilder cluster(Cluster cluster) {
    this.cluster = cluster;
    return this;
  }

  public Cluster cluster() {
    return cluster;
  }

  public StageBuilder hostCommands(HostCommand... hostCommands) {
    this.hostCommands.addAll(Arrays.asList(hostCommands));
    return this;
  }

  public Stage build() throws AmbariException {
    Stage stage = stageFactory.createNew(requestId,
      "/tmp/abari" + File.pathSeparator + requestId,
      cluster.getClusterName(),
      cluster.getClusterId(),
      requestContext,
      StageUtils.getGson().toJson(StageUtils.getClusterHostInfo(cluster)),
      StageUtils.getGson().toJson(new HashMap<String, String>()),
      StageUtils.getGson().toJson(customCommandExecutionHelper.createDefaultHostParams(cluster)));
    stage.setStageId(stageId);
    for (HostCommand each : hostCommands) {
      ActionExecutionContext exec = new ActionExecutionContext(
        cluster.getClusterName(),
        each.command.toString(),
        singletonList(new RequestResourceFilter(each.service, each.role.toString(), Arrays.asList(each.host))),
        Collections.<String, String>emptyMap());
      customCommandExecutionHelper.addExecutionCommandsToStage(exec, stage, Collections.emptyMap());
    }
    return stage;
  }
}
