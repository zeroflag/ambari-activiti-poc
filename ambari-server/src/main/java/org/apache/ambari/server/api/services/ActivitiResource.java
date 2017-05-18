package org.apache.ambari.server.api.services;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ambari.server.StaticallyInject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

/**
 * REST API for managing activiti workflows
 *
 * A typical client would look like this:
 *
 * process = startProcess()
 * while not process.ended():
 *  for each in process.userTasks():
 *    userInput = handleInput(each.forms)
 *    each.complete(userInput)
 */
@StaticallyInject
@Path("/activiti/")
public class ActivitiResource extends BaseService {
  private static final Gson GSON = new GsonBuilder().serializeNulls().create();
  @Inject
  private static WorkflowEngine workflowEngine;

  @POST
  @Path("/process")
  @Produces(MediaType.APPLICATION_JSON)
  public String startProcess() {
    return workflowEngine.startProcess();
  }

  @DELETE
  @Path("/process/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public void stopProcess(@PathParam("id") String processExecutionId) {
    workflowEngine.stopProcess(processExecutionId);
  }

  @GET
  @Path("/tasks/{id}")
  public Response userTasks(@PathParam("id") String processExecutionId) {
    List<UserTask> tasks = workflowEngine.getTasks(processExecutionId);
    return Response.ok(GSON.toJson(tasks), MediaType.TEXT_PLAIN)
      .header("Content-Type", "application/json")
      .build(); // XXX make sure null values are serialized
  }

  @GET
  @Path("/form/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserForm> forms(@PathParam("id") String taskId) {
    return workflowEngine.formData(taskId);
  }

  @POST
  @Path("/tasks/complete/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void completeUserTask(@PathParam("id") String taskId, Map<String, Object> variables) {
    workflowEngine.completeUserTask(taskId, variables);
  }

  @GET
  @Path("/process/ended/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public boolean processEnded(@PathParam("id") String processExecutionId) {
    return workflowEngine.processEnded(processExecutionId);
  }
}
