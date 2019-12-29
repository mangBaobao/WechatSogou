package cert.aiops.pega.service;

import cert.aiops.pega.bean.*;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.dao.HostStateDao;
import cert.aiops.pega.innerService.SystemInfoService;
import cert.aiops.pega.util.RedisClientUtil;
import cert.aiops.pega.util.SpringContextUtil;
import cert.aiops.pega.util.ZookeeperUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class FilterQueryServiceImpl implements  FilterQueryService{
    Logger logger= LoggerFactory.getLogger(FilterQueryServiceImpl.class);
    @Autowired
    private SystemInfoService systemInfoService;

    @Override
    public Future<FilterResponse> getUnavailBySystems(Date createdTime) {
        ObjectMapper objectMapper=new ObjectMapper();
        FilterResponse filterResponse=new FilterResponse();
        List<SystemInfo>  systemInfos=systemInfoService.getAllSystemNameAndId();
        Iterator<SystemInfo> iterator=systemInfos.iterator();
        HostStateDao hostStateDao= SpringContextUtil.getBean(HostStateDao.class);
        PegaConfiguration pegaConfiguration=SpringContextUtil.getBean(PegaConfiguration.class);
        long epoch= Long.parseLong(ZookeeperUtil.getInstance().getData(pegaConfiguration.getRoutineEpochPath()))-1;
        String finalKey="unavail_"+epoch;
        String result = RedisClientUtil.getInstance().getStr(finalKey);
        if(result!=null){
            logger.info("FilterQueryServiceImpl_getUnavailBySystems: find answers from cache; finalKey={}",finalKey);
            JavaType type = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, SystemQueryResponse.class);
            try {
                ArrayList<SystemQueryResponse> systemQueryResponses=objectMapper.readValue(result,type);
                filterResponse.setResponses(systemQueryResponses);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            while (iterator.hasNext()) {
                SystemInfo info = iterator.next();
                List<SinglePingState> hostStates = hostStateDao.getUnavailHostStates(info.getId(), epoch);
                if (hostStates.size() != 0) {
                    SystemQueryResponse systemQueryResponse = new SystemQueryResponse();
                    systemQueryResponse.setHosts(hostStates);
                    systemQueryResponse.setSystemName(info.getSystemName());
                    systemQueryResponse.setPage_number(1);
                    systemQueryResponse.setPage_size(hostStates.size());
                    systemQueryResponse.setTotalRecords(hostStates.size());
                    filterResponse.addReponse(systemQueryResponse);
                }
            }
            logger.info("FilterQueryServiceImpl_getUnavailBySystems: find answers from database; finalKey={}",finalKey);
            try {
                RedisClientUtil.getInstance().setStr(finalKey,objectMapper.writeValueAsString(filterResponse.getResponses()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            RedisClientUtil.getInstance().expire(finalKey,120);
        }
       return new AsyncResult<>(filterResponse);
    }
}
