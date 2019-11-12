package cert.aiops.pega.masterExecutors;

import cert.aiops.pega.bean.RequestTask;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.util.MessageUtil;
import cert.aiops.pega.util.ZookeeperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MessageQueueManager {
   private Logger logger = LoggerFactory.getLogger(MessageQueueManager.class);
   private ArrayList<String> queues;
   @Autowired
    PegaConfiguration pegaConfiguration;
   public MessageQueueManager(){
       queues=new ArrayList<>();
   }

   public boolean updateQueueStatus(){
       List<String> workers= ZookeeperUtil.getInstance().getChildren(pegaConfiguration.getWorkerPath());
       ArrayList<String> workersToBeDeleted=new ArrayList<>();
       boolean isQueueChange=false;
       if(!queues.isEmpty()) {
           for (String key : queues) {
               if (!workers.contains(key)) {
                   MessageUtil.getInstance().removeBinding(key, key);
                   MessageUtil.getInstance().deleteQueue(key);
                  workersToBeDeleted.add(key);
                   logger.info("MessageQueueManager_updateQueueStatus:about to remove queue name={}", key);
               }
           }
       }
       if(!workersToBeDeleted.isEmpty()) {
           queues.removeAll(workersToBeDeleted);
           isQueueChange=true;
           logger.info("MessageQueueManager_updateQueueStatus:success to remove all the queues");
       }
       for(String worker:workers){
           if(!queues.contains(worker)) {
               MessageUtil.getInstance().createQueue(worker);
               MessageUtil.getInstance().createBinding(worker,worker);
               queues.add(worker);
               isQueueChange=true;
               logger.info("MessageQueueManager_updateQueueStatus:add new queue name={}",worker);
           }
       }
       return isQueueChange;
   }
//    public boolean dispatchTaskToWorker(RequestTask task){
//       boolean oneTry=oneDispatchTaskToWorker(task);
//       boolean twoTry=oneDispatchTaskToWorker(task);
//       return oneTry || twoTry;
//    }

   public boolean dispatchTaskToWorker(RequestTask task){
       int max=queues.size();
//       max=(max-1)>0?max-1:max;
       int tryTimes=0;
       while(tryTimes<3) {
           int random = new Random().nextInt(max );
           logger.info("MessageQueueManager_dispatchTaskToWorker:random={},max={}",random,max);
           if(random>=max||random<0)
               random=max-1;
           if(isWorkerExists(random)){
               CorrelationData correlationData=new CorrelationData(task.getTaskId());
               MessageUtil.getInstance().sendMessage(queues.get(random),task,correlationData);
               logger.info("MessageQueueManager_dispatchTaskToWorker:send task {}  to worker={}",task.getTaskId(),queues.get(random));
               return true;
           }
           else
               tryTimes++;
       }
       logger.info("MessageQueueManager_dispatchTaskToWorker:send task to worker fail. cannot find exist worker");
       return false;
   }

   private boolean isWorkerExists(int index){
       String workerPath=pegaConfiguration.getWorkerPath();
       String workerName=queues.get(index);
       String wholePath=ZookeeperUtil.getInstance().concatPath(workerPath,workerName);
       return ZookeeperUtil.getInstance().checkExists(wholePath);
   }
}
