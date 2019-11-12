package cert.aiops.pega.service;

import cert.aiops.pega.bean.*;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.dao.HostStateDao;
import cert.aiops.pega.masterExecutors.Master;
import cert.aiops.pega.util.ExecResultTransformer;
import cert.aiops.pega.util.IdentityUtil;
import cert.aiops.pega.util.RedisClientUtil;
import cert.aiops.pega.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@Service
@PropertySource("classpath:utility.properties")
public class SystemQueryServiceImpl implements SystemQueryService {

    Logger logger = LoggerFactory.getLogger(SystemQueryServiceImpl.class);
    @Autowired
    private PegaConfiguration pegaConfiguration;

    @Value("${spring.cache.redis.time-to-live}")
    private int cacheDuration;

    @Override
    @Async("systemQueryExecutor")
    public Future<SystemQueryResponse> getSystemState(String systemName, int pageNumber, int pageSize, Date createdTime) {
        SystemQueryResponse response = new SystemQueryResponse();
        response.setPage_number(pageNumber);
        response.setSystemName(systemName);
        Master master = SpringContextUtil.getBean(Master.class);
        SystemInfo systemInfo = master.getSystemInfoByName(systemName);
        if(systemInfo == null){
            logger.info("SystemQueryServiceImpl_getSystemState: find no monitored system and going to return empty result. system name={}",systemName);
            return new AsyncResult<>(response);
        }
        long recentEpoch = master.getEpoch() - 1;
        long systemId = systemInfo.getId();
        String epochIdentity = IdentityUtil.generateEpochIdentity(recentEpoch,systemId);
        int recordSize= (int) RedisClientUtil.getInstance().getListSize(epochIdentity);
        logger.info("SystemQueryServiceImpl_getSystemState:epochIdentity={},listSize={}",epochIdentity,recordSize);
        if(recordSize==0) {
            HostStateDao hostStateDao = SpringContextUtil.getBean(HostStateDao.class);
            ArrayList<HostState> hostStates = hostStateDao.getHostStates(systemId, recentEpoch);
            ArrayList<SinglePingState> pingStates = ExecResultTransformer.hostState2PingState(hostStates, pegaConfiguration.getWorkingNet());
            RedisClientUtil.getInstance().batchAddList(epochIdentity, pingStates);
            RedisClientUtil.getInstance().expireObj(epochIdentity, cacheDuration);
            logger.info("SystemQuerySerivceImpl_getSystemState:select data from DB and put into cache: systemId={},epoch={},size={}", systemId, recentEpoch, pingStates.size());
        }
        recordSize= (int) RedisClientUtil.getInstance().getListSize(epochIdentity);
        int queridHead=(pageNumber-1)*pageSize;
        int queridTail=pageNumber*pageSize;
        queridHead=queridHead>recordSize?recordSize:queridHead;
        queridHead=queridHead<0?0:queridHead;
        queridTail=queridTail>recordSize?recordSize:queridTail;
        response.setPage_size(pageSize>recordSize?recordSize:pageSize);
        logger.info("SystemQueryServiceImpl_getSystemState:queridHead={},queridTail={}",queridHead,queridTail);
        List<SinglePingState> values=RedisClientUtil.getInstance().getListPage(epochIdentity,queridHead,queridTail);

        logger.info("SystemQueryServiceImpl_getSystemState:getListPage: epochIdentity={},value_size={}",epochIdentity,values.size() );
        if (values !=null || !values.isEmpty()) {
//            ArrayList<SinglePingState> pingStates = new ArrayList<>();
//            ObjectMapper objectMapper=new ObjectMapper();
//            for(Object singleValue:values){
//                try {
//                    SinglePingState pingState=objectMapper.readValue(singleValue,SinglePingState.class);
//                    pingStates.add(pingState);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            response.setHosts(pingStates);
            response.setHosts(values);
        } else {
//            HostStateDao dao = SpringContextUtil.getBean(HostStateDao.class);
//            SystemState systemState = dao.getCurrentSystemState(systemId, recentEpoch);
//            ArrayList<HostState> hostStates = systemState.getStateList();
//            String net = pegaConfiguration.getWorkingNet();
//            ArrayList<SinglePingState> pingStates = ExecResultTransformer.hostState2PingState(hostStates, net);
//            response.setTotalRecords(pingStates.size());
//            response.setHosts(pingStates);
//            response.setHosts(null);
            response.setHosts(null);
        }
        response.setTotalRecords(values.size());
        logger.info("SystemQueryServiceImpl_getSystemState: process complete : systemName={},epoch={}",systemName,epochIdentity );
        return new AsyncResult<>(response);
    }

//    @Override
//    @Async("systemQueryExecutor")
//    public Future<SystemQueryResponse> getSystemState(String systemName, int pageNumber, int pageSize, Date createdTime) {
//        logger.info("start to process system query");
//        SystemQueryResponse response =new SystemQueryResponse();
//        response.setPage_number(pageNumber);
//        response.setPage_size(pageSize);
//        response.setTotalRecords(32800);
//        ArrayList<HostState>  hostStates = new ArrayList<>();
//        for(int i = 0; i < 2;i++){
//            HostState state = new HostState();
//            state.setStatus(PegaEnum.Avail.unavail);
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String dateString = formatter.format(createdTime);
//            state.setUpdate_time(createdTime);
////            state.setNet(PegaEnum.Net.z);
//            state.setIp(String.valueOf(i));
//            hostStates.add(state);
//        }
//        response.setHosts(hostStates);
//        response.setSystemName(systemName);
//        logger.info("finish processing system query and return response");
//        logger.info(response.toString());
//        return new AsyncResult<>(response);
//    }
}
