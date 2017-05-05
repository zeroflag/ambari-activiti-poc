package org.apache.ambari.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ambari.server.orm.dao.RequestDAO;
import org.apache.ambari.server.orm.entities.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class PendingTasks {
  private static Logger LOG = LoggerFactory.getLogger(PendingTasks.class);
  @Inject private RequestDAO requestDAO;
  private Map<String,List<Long>> pendingTasks = new ConcurrentHashMap<>();
  private volatile boolean stopped = false;

  public synchronized void registerCommand(String activitiId, List<Long> requestIds) {
    pendingTasks.put(activitiId, new ArrayList<>(requestIds));
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
    Iterator<Map.Entry<String, List<Long>>> iter = pendingTasks.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, List<Long>> pendingTask = iter.next();
      String activityId = pendingTask.getKey();
      List<Long> requestIds = pendingTask.getValue();
      for (Iterator<Long> iterator = requestIds.iterator(); iterator.hasNext(); )
        removeIfCompleted(activityId, iterator);
      if (requestIds.isEmpty()) {
        LOG.info("Notifying activity: "+ activityId);
        iter.remove();                           // XXX HTC:1
        taskListener.taskCompleted(activityId);  // XXX HTC:2
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

}
