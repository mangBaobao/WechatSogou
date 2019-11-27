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

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RegistrationManager {
    private Logger logger = LoggerFactory.getLogger(RegistrationManager.class);
    private ConcurrentLinkedQueue<AgentException> arrivalExceptions;
    private HashMap<String, PegaEnum.IssueStatus> exceptionStatus;
    private ArrayList<HostInfo> hostInfos;
    // private ArrayList<RegisteredHost> registeredHosts;
    private HashMap<String, RegisteredHost> registeredHosts;
    private Date lastSynchronizedDBTime;
    HashMap<String, RegisteredHost> newArrival;
    @Autowired
    private PublishedHostService publishedHostService;

    @Autowired
    private HostInfoService hostInfoService;

    @Autowired
    private ProvinceUtil provinceUtil;

    @Autowired
    private PegaConfiguration pegaConfiguration;

    @Autowired
    private ClaimNoticeManager claimNoticeManager;

    public RegistrationManager() {
        arrivalExceptions = new ConcurrentLinkedQueue<>();
        registeredHosts = new HashMap<>();
        newArrival = new HashMap<>();
    }


    public void addAgentException(AgentException a) {
        arrivalExceptions.add(a);
    }

    public void storePublishedHosts(HashMap<String, RegisteredHost> registeredHosts) {
        Iterator iterator = registeredHosts.entrySet().iterator();
        ArrayList<RegisteredHost> hosts = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, RegisteredHost> entry = (Map.Entry<String, RegisteredHost>) iterator.next();
            hosts.add(entry.getValue());
        }
        publishedHostService.batchStoreHosts(hosts);
    }

    public void addAgentExceptionList(ArrayList<AgentException> agentExceptionList) {
        arrivalExceptions.addAll(agentExceptionList);
    }

    public void processExceptionIssues() {

    }

    public void firstPublishIdentification() {
        this.generateIdentifications();
        this.storePublishedHosts(registeredHosts);
        this.publishIdentifications(registeredHosts);
    }

    public void incPublishIdentification() {
        incAdmitHosts();
        if (newArrival.size() != 0) {
            storePublishedHosts(newArrival);
            this.publishIdentifications(newArrival);
            registeredHosts.putAll(newArrival);
        }
        newArrival.clear();
    }

    private void publishIdentifications(HashMap<String, RegisteredHost> registeredHosts) {
//        if(registeredHosts.size()==0)
//            generateIdentifications();
        ObjectMapper mapper = new ObjectMapper();
        SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter.serializeAllExcept("ip");
        FilterProvider filters = new SimpleFilterProvider().addFilter("PublishFilter", theFilter);
        String content = null;
        logger.info("publishIdentifications: begins to publish host identification. registered host count={}", registeredHosts.size());
        Iterator iterator = registeredHosts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RegisteredHost> entry = (Map.Entry<String, RegisteredHost>) iterator.next();
            RegisteredHost host = entry.getValue();
            String key = IdentityUtil.generateRegisterKey(host.getIp());
            try {
                content = mapper.writer(filters).writeValueAsString(host);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            RedisClientUtil.getInstance().setStr(key, content);
        }
        logger.info("publishIdentifications: finishes to publish host identification. registered host count={}", registeredHosts.size());
    }

    //incremental synchronize with mysql
    public void updateHostInfos() {

    }

    private void initiateHostInfos() {
        if (hostInfos != null)
            return;
        hostInfos = (ArrayList<HostInfo>) hostInfoService.getMaintainedHosts();
        lastSynchronizedDBTime = new Date();
    }

    private void generateIdentifications() {
        if (hostInfos == null)
            initiateHostInfos();
        String hostName;
        Date time = new Date();
        for (HostInfo hostInfo : hostInfos) {
            RegisteredHost host = new RegisteredHost();
            host.setIp(hostInfo.getIp());
            hostName = provinceUtil.getShortName(hostInfo.getHost_name().substring(0, 2));
            host.setHostName(IdentityUtil.generateRegisterName(pegaConfiguration.getWorkingNet(), hostName, host.getIp()));
            host.setId(null);
            host.setUpdate_time(time);
            host.addChannel(ChannelManager.__DEFAULT_CHANNEL);
            registeredHosts.put(host.getIp(), host);
        }
        logger.info("generateIdentifications: totally generate count={} host identifications", registeredHosts.size());
    }

    private void incAdmitHosts() {
        HashMap<String, ClaimNotice> claimNotices = claimNoticeManager.getLastRoundClaimNotices();
        if (claimNotices.size() == 0) {
            logger.info("registerHosts: find no claim notice, do nothing ...");
            return;
        }
        logger.info("registerHosts: find size={} new claim notice, start to process registration", claimNotices.size());
        Iterator iterator = claimNotices.entrySet().iterator();
        Date date = new Date();
        while (iterator.hasNext()) {
            Map.Entry<String, ClaimNotice> claim = (Map.Entry<String, ClaimNotice>) iterator.next();
            String ip = claim.getKey();
            RegisteredHost host = registeredHosts.get(ip);
            if (host.getId() == null) {
                host.setId(claim.getValue().getUuid());
                host.setUpdate_time(date);
                newArrival.put(ip, host);
                logger.info("registerHosts: register host ip={}, uuid={}", ip, host.getId());
            } else if (!host.getId().equals(claim.getValue().getUuid())) {
                logger.info("registerHost: find unequal id for host ip={}, register={},claim={},wait for exception process", ip, host.getId(), claim.getValue().getUuid());
            } else
                logger.info("registerHost: find equal id for host ip={},do nothing", ip, host.getId());
        }
        logger.info("registerHost: register size={} hosts successfully", newArrival.size());
    }
}
