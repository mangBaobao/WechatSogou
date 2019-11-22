package cert.aiops.pega.masterExecutors;

import cert.aiops.pega.startup.BeingMasterCondition;
import cert.aiops.pega.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Conditional(value = {BeingMasterCondition.class})
@EnableScheduling
@EnableAsync
public class MasterCronTasks {
    private Logger logger = LoggerFactory.getLogger(MasterCronTasks.class);
//    @Autowired
//    private PegaConfiguration pegaConfiguration;

    @Async("RenovationExecutor")
    @Scheduled(fixedDelay =60000, initialDelay = 100000)
    public void renovateMappings() {
        logger.info("MasterCronTasks_renovateMappings:cron task begins: renovateMappings");
        Date time = new Date();
        MessageQueueManager queueManager = SpringContextUtil.getBean(MessageQueueManager.class);
        boolean isQueueChanged=queueManager.updateQueueStatus();
        MappingManager mappingManager = SpringContextUtil.getBean(MappingManager.class);

        if(mappingManager.getWorkerSize()==0){
            logger.info("renovateMappings:no worker exist. Do nothing");
            return;
        }

        logger.info("MasterCronTasks_renovateMappings: checkAllocationFragment");
        if(mappingManager.checkAllocationFragment()){
            mappingManager.remappingAllSystems();
            logger.info("MasterCronTasks_renovateMappings ends");
            logger.info("cron task ends: renovateMappings");
            return;
        }
        boolean hasNewWorker=mappingManager.checkWorkersState();
        boolean isWorkerChanged = mappingManager.updateWorkerRecorders();
      boolean isSystemChanged = mappingManager.checkSystemState();
      boolean isAllTheSystemsAllocated=mappingManager.isAllTheSystemsAllocated();
      logger.info("MasterCronTasks_renovateMappings:isQueueChanged={},hasNewWorker={}," +
              "isWorkerModified={},isSystemModified={},isAllTheSystemAllocated={}",isQueueChanged,hasNewWorker,isWorkerChanged,
              isSystemChanged,isAllTheSystemsAllocated);
        if (!isSystemChanged && !isWorkerChanged&&!isQueueChanged&&!hasNewWorker&&isAllTheSystemsAllocated) {
            logger.info("cron task begins: renovateMappings:Nothing changed. Do nothing");
            return;
        }

        if(isSystemChanged==true){
            logger.info("MasterCronTasks_renovateMappings:isSystemChanged=true,begins to update systemRecorders");
            mappingManager.updateSystemRecorders();
        }

        if(isAllTheSystemsAllocated==false){
            logger.info("MasterCronTasks_renovateMappings: not all the systems are allocated, going to call remapping function to allocate systems to workers");
            mappingManager.remappingAllSystems();
            logger.info("cron task ends: renovateMappings");
            return;
        }

        if (isWorkerChanged==true || hasNewWorker==true) {
            logger.info("renovateMappings:  workerRecorders={},begins to allocate systems to worker", mappingManager.getWorkerIds());
//            if (isWorkerModified) {
                mappingManager.allocateSystemToWorkers(mappingManager.getSystemMappings(),mappingManager.getWorkerMappings());
                logger.info("renovateMappings_after allocateSystemToWorkers,workerMappings' size={}", mappingManager.getWorkerMappings().getSize());
                logger.info("renovateMappings_after allocateSystemToWorkers,workerMappings={}", mappingManager.getWorkerMappings().toTabbedString());
//            }
   //         mappingManager.loadBalanceAmongWorkers();
            mappingManager.rebalanceWorkerLoad();
            mappingManager.regenerateSystemMappings();
            logger.info("renovateMappings_after loadBalanceAmongWorkers,workerMappings' size={}", mappingManager.getWorkerMappings().getSize());
            logger.info("renovateMappings_after loadBalanceAmongWorkers,workerMappings={}", mappingManager.getWorkerMappings().toTabbedString());

        }

        mappingManager.writeMappingsToDB(time);
        mappingManager.writeWorkerMappingsToZKTree();
        mappingManager.writeSystemMappingsToZKTree();
        logger.info("cron task ends: renovateMappings");
    }

    @Async("EpochUpdateExecutor")
    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void updateEpoch() {
        Master master = SpringContextUtil.getBean(Master.class);
        master.updateEpoch();
        logger.info("cron task ends: updateEpoch");
    }


//    @Async("RenovationExecutor")
//    @Scheduled(fixedDelay = 300000, initialDelay = 480000)
//    public void rewriteAndStoreRoutineResult() {
//        Master master = SpringContextUtil.getBean(Master.class);
//        ArrayList<SystemInfo> systemInfos = master.getSystemInfos();
//        HostStateDao dao = SpringContextUtil.getBean(HostStateDao.class);
//        long epoch = master.getEpoch();
//        int hostCount;
//        for (SystemInfo systemInfo : systemInfos) {
//            long systemId = systemInfo.getId();
//            hostCount = 0;
//            String fuzzyKey = epoch + "_" + systemId+"*";
//            Set<String> keys = RedisClientUtil.getInstance().getFuzzyKeys(fuzzyKey);
//            //StringJoiner joiner = new StringJoiner(",");
//            if (keys != null || keys.size() != 0) {
//                for (String key : keys) {
//                    String execValue = RedisClientUtil.getInstance().getStr(key);
//                    execValue = ExecResultTransformer.rewriteExecResult(execValue);
//                    //joiner.add(execValue);
//                    RedisClientUtil.getInstance().setStr(key, execValue);
//                    RedisClientUtil.getInstance().expire(key, pegaConfiguration.getCacheValidation());
//                    ArrayList<HostState> hostStates = ExecResultTransformer.routineExecResulst2HostState(execValue, epoch, systemId);
//                    dao.putRoutineHostStateList(hostStates);
//                    hostCount += hostStates.size();
//                    RedisClientUtil.getInstance().delList(key);
//                }
//
//                logger.info("MasterCronTasks_rewriteAndStoreRoutineResult:store routine state successfully; epoch={},systemId={},count={}", epoch, systemId, hostCount);
//            }
//        }
//
//    }
}
