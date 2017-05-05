package org.apache.ambari.server.api.services;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.ambari.server.StaticallyInject;

import com.google.inject.Inject;

@StaticallyInject
@Path("/activiti/")
public class ActivitiResource extends BaseService {
  @Inject
  private static WorkflowEngine workflowEngine;

  @POST
  @Path("/process")
  @Produces(MediaType.APPLICATION_JSON)
  public String startProcess() {
    return workflowEngine.startProcess();
  }

  @GET
  @Path("/tasks")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserTask> userTasks() {
    return workflowEngine.getTasks();
  }

  @GET
  @Path("/process/ended/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public boolean taskEnded(@PathParam("id") String processId) {
    return workflowEngine.processEnded(processId);
  }

  @GET
  @Path("/form/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserForm> completeUserTask(@PathParam("id") String taskId) {
    return workflowEngine.formData(taskId);
  }

  @POST
  @Path("/tasks/complete/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void completeUserTask(@PathParam("id") String taskId, Map<String, Object> variables) {
    workflowEngine.completeUserTask(taskId, variables);
  }

}
