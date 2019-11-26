package cert.aiops.pega.registration;

import cert.aiops.pega.bean.AgentException;
import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.bean.RegisteredHost;
import cert.aiops.pega.channels.ChannelManager;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.service.HostInfoService;
import cert.aiops.pega.service.PublishedHostService;
import cert.aiops.pega.util.IdentityUtil;
import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.util.ProvinceUtil;
import cert.aiops.pega.util.RedisClientUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RegistrationManager {
    private Logger logger= LoggerFactory.getLogger(RegistrationManager.class);
    private ConcurrentLinkedQueue<AgentException> arrivalExceptions;
    private HashMap<String, PegaEnum.IssueStatus> exceptionStatus;
    private ArrayList<HostInfo> hostInfos;
    private ArrayList<RegisteredHost> registeredHosts;

    @Autowired
    private PublishedHostService publishedHostService;

    @Autowired
    private HostInfoService hostInfoService;

    @Autowired
    private ProvinceUtil provinceUtil;

    @Autowired
    private PegaConfiguration pegaConfiguration;

    public RegistrationManager(){
        arrivalExceptions=new ConcurrentLinkedQueue<>();
        registeredHosts=new ArrayList<>();
    }


    public void addAgentException(AgentException a){
        arrivalExceptions.add(a);
    }

    public void storePublishedHosts(){
        publishedHostService.batchStoreHosts(registeredHosts);
    }

    public void addAgentExceptionList(ArrayList<AgentException> agentExceptionList){
        arrivalExceptions.addAll(agentExceptionList);
    }

    public void processExceptionIssues(){

    }

    public void publishIdentifications(){
        if(registeredHosts.size()==0)
            generateIdentifications();
        ObjectMapper mapper=new ObjectMapper();
        SimpleBeanPropertyFilter theFilter=SimpleBeanPropertyFilter.serializeAllExcept("ip");
        FilterProvider filters=new SimpleFilterProvider().addFilter("PublishFilter",theFilter);
        String content=null;
        logger.info("publishIdentifications: begins to publish host identification. registered host count={}",registeredHosts.size());
        for(RegisteredHost host:registeredHosts){
            String key=IdentityUtil.generateRegisterKey(host.getIp());
            try {
                content=mapper.writer(filters).writeValueAsString(host);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            RedisClientUtil.getInstance().setStr(key,content);
        }
        logger.info("publishIdentifications: finishes to publish host identification. registered host count={}",registeredHosts.size());
    }

    public void updateHostInfos(){

    }

    private void initiateHostInfos(){
        if(hostInfos!=null)
            return;
        hostInfos= (ArrayList<HostInfo>) hostInfoService.getMaintainedHosts();
    }

    private void generateIdentifications(){
        if(hostInfos==null)
            initiateHostInfos();
        String hostName;
        Date time=new Date();
        for(HostInfo hostInfo:hostInfos){
            RegisteredHost host=new RegisteredHost();
            host.setIp(hostInfo.getIp());
            hostName=provinceUtil.getShortName(hostInfo.getHost_name().substring(0,2));
            host.setHostName(IdentityUtil.generateRegisterName(pegaConfiguration.getWorkingNet(),hostName,host.getIp()));
            host.setId(null);
            host.setUpdate_time(time);
            host.addChannel(ChannelManager.__DEFAULT_CHANNEL);
            registeredHosts.add(host);
        }
        logger.info("generateIdentifications: totally generate count={} host identifications", registeredHosts.size());
    }

    public void registerHosts(){

    }
}
