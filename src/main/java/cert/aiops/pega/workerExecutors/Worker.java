package cert.aiops.pega.workerExecutors;


import cert.aiops.pega.bean.*;
import cert.aiops.pega.bean.mapping.WorkAssignment;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.config.WorkerConfiguration;
import cert.aiops.pega.dao.HostStateDao;
import cert.aiops.pega.service.HostInfoService;
import cert.aiops.pega.service.SystemInfoService;
import cert.aiops.pega.startup.BeingWorkerCondition;
import cert.aiops.pega.util.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
//import javafx.util.Pair;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Component
@EnableAsync
@PropertySource(value = {"classpath:worker.properties"})
@Conditional(value = {BeingWorkerCondition.class})
public class Worker {
    private Logger logger = LoggerFactory.getLogger(Worker.class);
    private ArrayList<WorkAssignment> workAssignments;
    private ArrayList<SystemInfo> systemInfos;
    private int hostCount = 0;
    private String workerId;
    private Stat assignmentStat=null;
    private int routineFileNumber;

    @Autowired
    private WorkerConfiguration workerConfiguration;
    @Autowired
    private PegaConfiguration pegaConfiguration;

    public Worker() {
        workAssignments = new ArrayList<>();
        systemInfos = new ArrayList<>();
    }

    public void init() {
        workerId=pegaConfiguration.getId();
        registerToZK();
    }

    private void registerToZK() {
        String workerPath = pegaConfiguration.getWorkerPath();
        String nodePath = ZookeeperUtil.getInstance().concatPath(workerPath, workerId);
        String content = workerConfiguration.getQueueName() + ";" + workerConfiguration.getQueueRoutingKey();
        ZookeeperUtil.getInstance().createEphemeralNode(nodePath, content);
    }

    ArrayList<Long> getSystemInfoIds(){
        ArrayList<Long> ids= new ArrayList<>();
        for(SystemInfo info:systemInfos)
            ids.add(info.getId());
        return ids;
    }

    void loadWorkAssignmentsFromZKTree(){
        String mappingPath=pegaConfiguration.getMappingPath();
        String nodePath=ZookeeperUtil.getInstance().concatPath(mappingPath,workerId);
        String assignments=ZookeeperUtil.getInstance().getData(nodePath);
        if(assignments.isEmpty())
            return;
        ObjectMapper mapper=new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, WorkAssignment.class);
        try {
            workAssignments=mapper.readValue(assignments,type);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void setAssignmentStat(Stat stat){
        assignmentStat=stat;
    }

    Stat getAssignmentStat(){
        return assignmentStat;
    }

    private void loadSystemInfoFromDB() {
        SystemInfoService systemInfoService = SpringContextUtil.getBean(SystemInfoService.class);
        HostInfoService hostInfoService = SpringContextUtil.getBean(HostInfoService.class);
        for (WorkAssignment assignment : workAssignments) {
            SystemInfo systemInfo = getSystemInfoFromList(assignment.getSystemId());
            if (systemInfo == null) {
                systemInfo = systemInfoService.getSystemInfoById(assignment.getSystemId());
                systemInfos.add(systemInfo);
            }
            List<HostInfo> hostInfos = hostInfoService.getHostsBySystemIdAndIdRange(
                    assignment.getSystemId(), assignment.getHeader(), assignment.getTrailer());
            systemInfo.getHosts().addAll(hostInfos);
            hostCount += assignment.getCount();
        }
    }

    private SystemInfo getSystemInfoFromList(long systemId) {
        for (SystemInfo systemInfo : systemInfos)
            if (systemInfo.getId() == systemId)
                return systemInfo;
        return null;
    }

    void generateRoutineFiles() {
        loadSystemInfoFromDB();
        int maxCount = workerConfiguration.getMaxCountPerFile();
        int slotCount = workerConfiguration.getConcurrency();
        int countPerSlot = hostCount / slotCount;
        String routinePath = workerConfiguration.getRoutineFilePath();
       // prepareDirPath(routinePath);
        sortSystemInfoByHostSize();
        Iterator<SystemInfo> iterator = systemInfos.iterator();

        int fileNum=0;
        int spare = countPerSlot;
        int currentSlot = 0;
        long headId;
        long endId;
        HostInfo head;
        int leftHostCount = 0;
        SystemInfo info = null;
        boolean getNext = true;
        while (iterator.hasNext()) {
            if (getNext) {
                info = iterator.next();
                Sorter<HostInfo> sorter = new Sorter<>();
                sorter.setCandidates(info.getHosts());
                sorter.getSortedCandidates();
                leftHostCount = info.getHostCount();
            }
            if (spare <= 0) {
                currentSlot += 1;
                spare = countPerSlot;
            }
            if (currentSlot > slotCount)
                break;

            head = info.getHosts().get(info.getHostCount() - leftHostCount);
            if (head != null)
                headId = head.getId();
            else {
                logger.info("generateRoutineFiles:head info =null; system id={},lefthostcount={}. turn to next system info", info.getId(), leftHostCount);
                getNext = true;
                continue;
            }

            int threshold;
            if(leftHostCount<=spare) {
                getNext = true;
                threshold=leftHostCount;
            }
            else {
                getNext = false;
                threshold=spare;
            }

            while(threshold>0){
                int cutCount = threshold > maxCount ? maxCount : threshold;
                endId = info.getHostInfo(headId, cutCount - 1).getId();
                ArrayList<HostInfo> collectedHosts = info.getHosts(headId, endId);
                String fileName = IdentityUtil.generateFileName(info.getId() , headId ,endId);
                generateSingleRoutineFile(routinePath, currentSlot, fileName, extractIps(collectedHosts));
                fileNum+=1;
                head = info.getHostInfo(headId, cutCount);
                if (head != null)
                    headId = head.getId();
                else {
                    logger.info("generateRoutineFiles:head info =null; system id={},lefthostcount={}. turn to next system info", info.getId(), leftHostCount);
                    break;
                }
                threshold -= cutCount;
                leftHostCount-=cutCount;
                spare-=cutCount;
            }

//            if (leftHostCount <= spare) {
//                while (leftHostCount > 0) {
//                    int cutCount = leftHostCount > maxCount ? maxCount : leftHostCount;
//                    endId = info.getHostInfo(headId, cutCount - 1).getId();
//                    ArrayList<HostInfo> collectedHosts = info.getHosts(headId, endId);
//                    String fileName = info.getId() + "_" + headId + "_" + endId;
//                    generateSingleRoutineFile(routinePath, currentSlot, fileName, extractIps(collectedHosts));
//                    head = info.getHostInfo(headId, cutCount);
//                    if (head != null)
//                        headId = head.getId();
//                    else {
//                        logger.info("generateRoutineFiles:head info =null; system id={},lefthostcount={}. turn to next system info", info.getId(), leftHostCount);
//                        break;
//                    }
//                    leftHostCount -= cutCount;
//                }
//                spare = spare - info.getHostCount();
//                getNext = true;
//            } else {
//                while (spare > 0) {
//                    int cutCount = spare > maxCount ? maxCount : spare;
//                    endId = info.getHostInfo(headId, cutCount - 1).getId();
//                    ArrayList<HostInfo> collectedHosts = info.getHosts(headId, endId);
//                    String fileName = info.getId() + "_" + headId + "_" + endId;
//                    generateSingleRoutineFile(routinePath, currentSlot, fileName, extractIps(collectedHosts));
//                    head = info.getHostInfo(headId, cutCount);
//                    if (head != null)
//                        headId = head.getId();
//                    spare -= cutCount;
//                    leftHostCount -= cutCount;
//                }
//                getNext = false;
//            }
        }
        this.routineFileNumber=fileNum;
    }

    private ArrayList<String> extractIps(ArrayList<HostInfo> hostInfos) {
        ArrayList<String> ips = new ArrayList<>();
        for (HostInfo host : hostInfos) {
            ips.add(host.getIp());
        }
        return ips;
    }

    void prepareDirPath(String parentDirPath){
        File parentDir = new File(parentDirPath);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
            return;
        }
        File[] files = parentDir.listFiles();
        if(files!=null){
            for(File file:files){
                if(file.isDirectory())
                    prepareDirPath(file.getAbsolutePath());
                else
                    file.delete();
            }
        }

    }

    private void generateSingleRoutineFile(String parentDirPath, int currentSlot, String fileName, ArrayList<String> ips) {

        String currentSlotPath = parentDirPath + File.pathSeparator + currentSlot;
        File currentDir = new File(currentSlotPath);
        if (!currentDir.exists())
            currentDir.mkdirs();
//        ArrayList<File> fileArrayList = findFileByName(pareDir, fileName);
//        for (File file : fileArrayList) {
//            file.delete();
//        }
        String fileWholePath = currentSlotPath + File.pathSeparator + fileName;
        writeIps2File(fileWholePath, ips);
    }

//    private ArrayList<File> findFileByName(File parentDir, String fileName) {
//        ArrayList<File> wanted = new ArrayList<>();
//        File[] files = parentDir.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isDirectory())
//                    wanted.addAll(findFileByName(file, fileName));
//                if (file.getName().equals(fileName))
//                    wanted.add(file);
//            }
//        }
//        return wanted;
//    }

    private void sortSystemInfoByHostSize() {
        Collections.sort(systemInfos, (Comparator) (o1, o2) -> {
            if (o1 instanceof SystemInfo && o2 instanceof SystemInfo) {
                SystemInfo info1 = (SystemInfo) o1;
                SystemInfo info2 = (SystemInfo) o2;
                if (info1.getHostCount() > info2.getHostCount())
                    return -1;
                if (info1.getHostCount() == info2.getHostCount())
                    return 0;
                return 1;
            }
            throw new ClassCastException("sortSystemInfoByHostSize:cannot transform Type Object to SystemInfo");
        });
    }

    private void writeIps2File(String path, ArrayList<String> ips) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
            for (int i = 0; i < ips.size(); i++) {
                bufferedWriter.write(ips.get(i));
                bufferedWriter.newLine();
                if (i % 1000 == 0)
                    bufferedWriter.flush();
            }
            bufferedWriter.flush();
            logger.info("writeIps2File:writeFile success. fileWholePath={}", path);
            logger.debug("writeIps2File:writeFile success. fileWholePath={},ips={}", path, ips.toString());
        } catch (IOException e) {
            logger.info("writeIps2File: writeFile fail. fileName={},reason={}", path, e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateRequestFile(String taskId, ArrayList<String> ips) {
        String requestPath = workerConfiguration.getRequestFilePath();
        File requestPathFile = new File(requestPath);
        if (!requestPathFile.exists())
            requestPathFile.mkdirs();
        String requestWholePath = requestPath + File.pathSeparator + taskId;
        writeIps2File(requestWholePath, ips);
    }

    boolean executePing(String taskId, PegaEnum.TaskType type, int waitTime,int threshold) {
      String taskType= String.valueOf(type);
        String pingScript = workerConfiguration.getPingScriptPath();
        Process process=null;
        try {
            int retryTimes = workerConfiguration.getRetryTimes();
            int retry = 1;
            int key_num;

            if(type== PegaEnum.TaskType.request)
                key_num=1;
            else
                key_num=this.routineFileNumber;

            Set<String> keys;
            while (retryTimes >= retry) {
                 process = Runtime.getRuntime().exec(new String[]{"/bin/bash", pingScript, " -t " +taskType+" -k " + taskId});
                 int currentWait=waitTime;
                 while(currentWait<threshold) {
                     process.wait(waitTime * retry);
                     String keyPattern = taskId + "_" + workerId + "*";
                     keys = RedisClientUtil.getInstance().getFuzzyKeys(keyPattern);
                     if (keys != null && keys.size() == key_num) {
                         logger.info("executePing: exec success. key={},taskType={},retryTimes={}", keys.iterator().next(), taskType, retry);
                         return true;
                     }
                     currentWait+=waitTime;
                 }
                retry++;
            }
            logger.info("executePing: exec fail. taskId={},taskType={},retryTimes={}", taskId, taskType, retry);
            return false;
        } catch (IOException e) {
            logger.info("executePing: exec Fail. taskId={},taskType={},reason={}", taskId, taskType, e.getCause());
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.info("executePing: wait Fail. taskId={},taskType={},reason={}", taskId, taskType, e.getCause());
            e.printStackTrace();
        }finally{
            if(process != null){
                process.destroy();
            }
        }
        return  false;
    }

    public String getWorkerId(){
        return  workerId;
    }

    @Async("RequestHandleExecutor")
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${worker.queue.name}", durable = "true"), exchange = @Exchange(value = "spring.rabbitmq.exchange.name"),
            key = "${worker.queue.routingKey}"), containerFactory = "manualAckContainerFactory")
    @RabbitHandler
    public void execRequestTask(@Payload RequestTask task, @Headers Map<String, Object> headers, Channel channel) {
        logger.info("payLoad:{}", task.toString());
        logger.info("headers:{}", headers);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        logger.info("deliveryTag:{}", deliveryTag);
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        generateRequestFile(task.getTaskId(), task.getIplist());
        boolean execResult= executePing(task.getTaskId(),PegaEnum.TaskType.request,workerConfiguration.getRequestWaitBase(),workerConfiguration.getRequestWaitThreshold());
        if(!execResult) {
            logger.info("execRequestTask: task Fail. task id={}",task.getTaskId());
          return;
        }
        String keyPattern=task.getTaskId()+"_"+workerId+"*";
        Set<String> keys=RedisClientUtil.getInstance().getFuzzyKeys(keyPattern);
        ArrayList<SinglePingState> pingStates= refreshHostStateInCache(keys);
        storeHostState2DB(task.getTaskId(),pingStates);
    }

    private void storeHostState2DB(String taskId, ArrayList<SinglePingState> pingStates){
        ArrayList<HostState> hostStates= ExecResultTransformer.pingState2RequestHostState(pingStates, taskId);
        HostStateDao dao= SpringContextUtil.getBean(HostStateDao.class);
        dao.putRequestHostStateList(hostStates);
    }

    private ArrayList<SinglePingState> refreshHostStateInCache(Set<String> keys){
        ArrayList<SinglePingState>  pingStates=new ArrayList<>();
        for(String key:keys){
            String state=RedisClientUtil.getInstance().getStr(key);
            state= ExecResultTransformer.rewriteExecResult(state);
            RedisClientUtil.getInstance().setStr(key,state);
            pingStates.addAll(ExecResultTransformer.execResult2PingState(state));
        }
        return pingStates;
    }

//    //todo routine detect managed hosts  and write  successful result into redis and database, and also  write failed result into database
//    @CachePut(value = "routineTask", keyGenerator = "routineCacheKeyGenerator")
//    @Deprecated
//    public SystemState routineDetect() {
//        return null;
//    }
//
//    //todo after routine detect and right before write result to redis , count  detectd host number and send them to redis as well
//    @CachePut(value = "routineTask", key = "#p0+'_'+#p1+'_count'")
//    @Deprecated
//    public Pair<String, Integer> calRoutineCount(long epoch, String systemId) {
//        return null;
//    }
}
