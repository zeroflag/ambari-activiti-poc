package org.apache.ambari.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.ambari.server.orm.dao.RequestDAO;
import org.apache.ambari.server.orm.entities.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class PendingTasks {
  private static Logger LOG = LoggerFactory.getLogger(PendingTasks.class);
  @Inject private RequestDAO requestDAO;
  private List<PendingTask> pendingTasks2 = new ArrayList<>();
  private volatile boolean stopped = false;

  public synchronized void add(String processExecutionId, String activitiId, List<Long> requestIds) {
    pendingTasks2.add(new PendingTask(processExecutionId, activitiId, new ArrayList<>(requestIds)));
  }

  public void startCheckingTaskCompletion(TaskListener taskListener) {
    Thread thread = new Thread(() -> {
      while (!stopped) {
        try {
          notifyOnTaskCompletion(taskListener);
          Thread.sleep(200);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        } catch (Exception e) {
          LOG.error("Error", e);
        }
      }
    });
    thread.start();
  }

  private void notifyOnTaskCompletion(TaskListener taskListener) {
    Iterator<PendingTask> iter = pendingTasks2.iterator();
    while (iter.hasNext()) {
      PendingTask pendingTask = iter.next();
      for (Iterator<Long> iterator = pendingTask.requestId.iterator(); iterator.hasNext(); )
        removeIfCompleted(pendingTask.activitiId, iterator);
      if (pendingTask.requestId.isEmpty()) {
        LOG.info("Notifying activity: "+ pendingTask.activitiId);
        iter.remove();                           // XXX HTC:1
        taskListener.taskCompleted(pendingTask.activitiId);  // XXX HTC:2
      }
    }
  }

  private void removeIfCompleted(String activityId, Iterator<Long> iterator) {
    Long requestId = iterator.next();
    if (requestId == null) {
      LOG.info("Command completed: {} activitiId: {}", requestId, activityId);
      iterator.remove();
    } else if (isCompleted(requestId)) {
      LOG.info("Command completed: {} activitiId: {}", requestId, activityId);
      iterator.remove();
    }
  }

  private boolean isCompleted(Long requestId) {
    RequestEntity requestEntity = requestDAO.findByPks(Arrays.asList(requestId), true).get(0);
    return requestEntity.getStatus().isCompletedState();
  }

  public List<Long> pendingRequestIds(String processExecutionId) {
    return pendingTasks2.stream()
      .filter(each -> each.processExecutionId.equals(processExecutionId))
      .map(each -> each.requestId)
      .flatMap(c -> c.stream()).collect(Collectors.toList());
  }
}
