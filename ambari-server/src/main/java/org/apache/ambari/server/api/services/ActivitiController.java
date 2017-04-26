package org.apache.ambari.server.api.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.activiti.engine.form.FormProperty;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.Role;
import org.apache.ambari.server.RoleCommand;
import org.apache.ambari.server.WorkflowApi;

@Path("/activiti/")
public class ActivitiController extends BaseService {
  private ActivitiService activitiService;

  public ActivitiController() {
    this.activitiService = new ActivitiService();
  }

  @POST
  @Path("/process")
  @Produces(MediaType.APPLICATION_JSON)
  public String startProcess() {
    try {
//      WorkflowApi.getInstance().sendInstallCommand("HDFS", "ZKFC", "c6401.ambari.apache.org");
//      WorkflowApi.getInstance().sendCommandToComponent("HDFS", "ZKFC", "c6401.ambari.apache.org", RoleCommand.INSTALL);
      WorkflowApi.getInstance().sendCommandToService("ZOOKEEPER", RoleCommand.STOP);
    } catch (AmbariException e) {
      e.printStackTrace();
    }

    return activitiService.startProcess().getId();
  }

  @GET
  @Path("/tasks")
  @Produces(MediaType.APPLICATION_JSON)
  public List<TaskRepresentation> userTasks() {
    return activitiService
      .getTasks()
      .stream()
      .map(task -> new TaskRepresentation(task.getId(), task.getName()))
      .collect(Collectors.toList());
  }

  @GET
  @Path("/process/ended/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public boolean taskEnded(@PathParam("id") String processId) {
    return activitiService.processEnded(processId);
  }

  @GET
  @Path("/form/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<FormProperty> completeUserTask(@PathParam("id") String taskId) {
    return activitiService.formData(taskId);
  }

  @POST
  @Path("/tasks/complete/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void completeUserTask(@PathParam("id") String taskId, Map<String, Object> variables) {
    activitiService.completeUserTask(taskId, variables);
  }

  static class TaskRepresentation {
    private final String id;
    private final String name;

    public TaskRepresentation(String id, String name) {
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
}
