package cert.aiops.pega.service;

import cert.aiops.pega.bean.*;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.masterExecutors.MessageQueueManager;
import cert.aiops.pega.util.ExecResultTransformer;
import cert.aiops.pega.util.RedisClientUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Future;

@Service
public class HostQueryServiceImpl implements HostQueryService {
    private Logger logger = LoggerFactory.getLogger(HostQueryServiceImpl.class);

    @Autowired
    private MessageQueueManager messageQueueManager;
    @Autowired
    private PegaConfiguration pegaConfiguration;

    @Override
    @Async("hostQueryExecutor")
    public Future<RequestTaskResponse> getHostState(String network, String[] ips, Date createdTime) {
        // logger.info("start to process host query");
        RequestTaskResponse response = new RequestTaskResponse();
        ArrayList<SinglePingState> pingStates = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        RequestTask task = new RequestTask();
        String taskId = String.valueOf(createdTime.getTime());
        ArrayList<String> hostIps = new ArrayList<>(Arrays.asList(ips));
        task.setIplist(hostIps);
        task.setTaskId(taskId);
        int retry = 1;
        int maxRetry = pegaConfiguration.getRequestRetryTimes();
        int requestWaitBase = pegaConfiguration.getRequestWaitBase();

        messageQueueManager.dispatchTaskToWorker(task);
        while (retry <= maxRetry) {
            try {
                logger.info("HostQueryServiceImpl_getHostState: retry={},requestWaitBase={},maxRetry={}",retry,requestWaitBase,maxRetry);
                Thread.sleep(retry * requestWaitBase);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String keyPattern=taskId+"*";
            Set<String> keys = RedisClientUtil.getInstance().getFuzzyKeys(keyPattern);
           logger.info("HostQueryServiceImpl_getHostState: getFuzzyKeys:key num={} by taskId={}",keys.size(),keyPattern);
            if (keys != null && keys.size() != 0) {
                String key = keys.iterator().next();
                String value = RedisClientUtil.getInstance().getStr(key);
       //         logger.info("HostQueryServiceImpl_getHostState: getStringFromRedis:value={} by taskId={}",value,taskId);
                value = ExecResultTransformer.rewriteExecResult(value);
       //         logger.info("HostQueryServiceImpl_getHostState: rewriteExecResult:value={} by taskId={}",value,taskId);
                JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, SinglePingState.class);
                try {
                    pingStates = mapper.readValue(value, type);
        //            logger.info("HostQueryServiceImpl_getHostState:get ping states from cache; taskId={},key={}", taskId, key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RedisClientUtil.getInstance().delStr(key);
                break;
            }
            retry++;
        }
        response.setHosts(pingStates);
           logger.info("HostQueryServiceImpl_getHostState:finishes to process host query,taskId={}", taskId);
        return new AsyncResult<>(response);
    }

//    @Override
//    @Async("hostQueryExecutor")
//    public Future<RequestTaskResponse> getHostState(String network, String[] ips, Date createdTime) {
//        logger.info("start to process host query");
//        RequestTaskResponse response = new RequestTaskResponse();
//        ArrayList<SinglePingState> hosts = new ArrayList<>();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        for (int i = 0; i < ips.length; i++) {
//            SinglePingState host = new SinglePingState();
////            host.setNet(PegaEnum.Net.v);
//            host.setIp(ips[i]);
////            host.setCreatedTime(createdTime);
////            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////            String dateString = formatter.format(createdTime);
////            host.setTimestamp(dateString);
//            host.setUpdate_time(formatter.format(createdTime));
//            host.setStatus(PegaEnum.Avail.unavail);
//            hosts.add(host);
//        }
//        response.setHosts(hosts);
//        logger.info("finishes to process host query");
//        return new AsyncResult<>(response);
//    }
}
