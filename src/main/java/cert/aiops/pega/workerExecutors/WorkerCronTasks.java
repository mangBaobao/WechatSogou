package cert.aiops.pega.workerExecutors;

import cert.aiops.pega.bean.HostState;
import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.bean.SinglePingState;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.config.WorkerConfiguration;
import cert.aiops.pega.dao.HostStateDao;
import cert.aiops.pega.startup.BeingWorkerCondition;
import cert.aiops.pega.util.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Conditional(value = {BeingWorkerCondition.class})
@EnableScheduling
@EnableAsync
public class WorkerCronTasks {
    private Logger logger = LoggerFactory.getLogger(WorkerCronTasks.class);
    private static long epoch = -1;
    @Autowired
    PegaConfiguration pegaConfiguration;
    @Autowired
    WorkerConfiguration workerConfiguration;

    @Async
    @Scheduled(fixedDelay = 3600000, initialDelay = 100000)
    public void getWorkAssignment() {
        String mappingPath = pegaConfiguration.getMappingPath();
        String workerId = pegaConfiguration.getId();
        String nodePath = ZookeeperUtil.getInstance().concatPath(mappingPath, workerId);
        Stat nodeStat = ZookeeperUtil.getInstance().getNodeStat(nodePath);
        Worker worker = SpringContextUtil.getBean(Worker.class);
        Stat currentStat = worker.getAssignmentStat();
        if (currentStat == null || currentStat.getAversion() != nodeStat.getAversion()) {
            logger.info("WorkerCronTasks_getWorkAssignment:update work assignment version from" +
                    " {} to {}", currentStat.getAversion(), nodeStat.getAversion());
            worker.setAssignmentStat(nodeStat);
            logger.info("WorkerCronTasks_getWorkAssignment:begins to update work assignments");
            worker.loadWorkAssignmentsFromZKTree();
            logger.info("WorkerCronTasks_getWorkAssignment:finishes to update work assignments");
            logger.info("WorkerCronTasks_getWorkAssignment:clear all the existing work assignments.");
            worker.prepareDirPath(workerConfiguration.getRoutineFilePath());
            logger.info("WorkerCronTasks_getWorkAssignment:begins to generate routine files in designated path");
            worker.generateRoutineFiles();
            logger.info("WorkerCronTasks_getWorkAssignment:finishes to generate routine files in designated path");

        }
    }

    @Async
    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void invokeRoutineTask() {
        String epochPath = pegaConfiguration.getRoutineEpochPath();
        long treeEpoch = Long.parseLong(ZookeeperUtil.getInstance().getData(epochPath));
        if (treeEpoch != epoch) {
            logger.info("WorkerCronTasks_invokeRoutineTask:update epoch from {} to {}", epoch, treeEpoch);
            epoch = treeEpoch;
            Worker worker = SpringContextUtil.getBean(Worker.class);
            logger.info("WorkerCronTasks_invokeRoutineTask:begins to execute routine ping task with epoch={}", epoch);
            worker.executePing(String.valueOf(epoch), PegaEnum.TaskType.routine, workerConfiguration.getRoutineWaitBase(),workerConfiguration.getRoutineWaitThreshold());
            logger.info("WorkerCronTasks_invokeRoutineTask:finishes to execute routine ping task with epoch={}", epoch);

            logger.info("WorkerCronTasks_invokeRoutineTask: begines to collect and store execution Result ");
           // ObjectMapper objectMapper = new ObjectMapper();
            HostStateDao dao = SpringContextUtil.getBean(HostStateDao.class);
            ArrayList<Long> systemIds = worker.getSystemInfoIds();
            for (long id : systemIds) {
                String fuzzyKey = IdentityUtil.generateFuzzyIdentity(epoch, id);
                String finalKey = IdentityUtil.generateEpochIdentity(epoch, id);
                Set<String> keys = RedisClientUtil.getInstance().getFuzzyKeys(fuzzyKey);
                for (String key : keys) {
                    String execValue = RedisClientUtil.getInstance().getStr(key);
                    execValue = ExecResultTransformer.rewriteExecResult(execValue);
                    List<SinglePingState> pingStates = ExecResultTransformer.execResult2PingState(execValue);
                    ArrayList<HostState> hostStates = ExecResultTransformer.pingState2RoutineHostState((ArrayList<SinglePingState>) pingStates, epoch, id);
                    dao.putRoutineHostStateList(hostStates);
                    logger.info("WorkerCronTasks_invokeRoutineTask: finishes to store execution results in DB; systemId={},count={}", id, hostStates.size());
                    RedisClientUtil.getInstance().batchAddList(finalKey, pingStates);
                    RedisClientUtil.getInstance().delStr(key);
                    logger.info("WorkerCronTasks_invokeRoutineTask: finishes to cache execution results in zSet ; key={},count={}", finalKey, pingStates.size());
                }
            }
        }
    }
}
