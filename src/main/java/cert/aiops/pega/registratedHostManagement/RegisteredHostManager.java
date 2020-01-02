package cert.aiops.pega.registratedHostManagement;

import cert.aiops.pega.bean.Judgement;
import cert.aiops.pega.bean.RegistrationException;
import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.bean.RegisteredHost;
import cert.aiops.pega.channels.ChannelManager;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.innerService.HostInfoService;
import cert.aiops.pega.innerService.JudgementService;
import cert.aiops.pega.innerService.RegisteredHostService;
import cert.aiops.pega.innerService.RegistrationExceptionService;
import cert.aiops.pega.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Order(1)
@Component
public class RegisteredHostManager {
    private Logger logger = LoggerFactory.getLogger(RegisteredHostManager.class);

    private final String __EXTRACT = "extractUuid";
    private final String __LOCAL = "localIdentity";
    private final String __PUBLISH = "publishedUuid";

    private ConcurrentLinkedQueue<RegistrationException> arrivalExceptions;
    private HashMap<String, PegaEnum.IssueStatus> exceptionStatus;//key:issueId,value:currentIssueState
    private ArrayList<HostInfo> hostInfos;
    // private ArrayList<RegisteredHost> registeredHosts;
    private HashMap<String, RegisteredHost> registeredHosts;//key:ip,value:host instance
    private HashMap<String, String> id2IpMap;
    private Date lastReadHostInfos;
    private Date latestRegisteredTime = null;
    private HashMap<String, RegisteredHost> newArrival;
    private HashMap<String, RegisteredHost> updatedHostsRecently;//key:ip,value:host instance
    @Autowired
    private RegisteredHostService registeredHostService;

    @Autowired
    private HostInfoService hostInfoService;

    @Autowired
    private RegistrationExceptionService registrationExceptionService;

    @Autowired
    private ProvinceUtil provinceUtil;

    @Autowired
    private PegaConfiguration pegaConfiguration;

    @Autowired
    private ClaimNoticeManager claimNoticeManager;

    @Autowired
    private JudgementService judgementService;

    public RegisteredHostManager() {
        arrivalExceptions = new ConcurrentLinkedQueue<>();
        registeredHosts = new HashMap<>();
        newArrival = new HashMap<>();
        exceptionStatus = new HashMap<>();
        id2IpMap = new HashMap<>();
    }


    public void addAgentException(RegistrationException a) {
        arrivalExceptions.add(a);
    }

    public void storePublishedHosts(HashMap<String, RegisteredHost> registeredHosts) {
        Iterator iterator = registeredHosts.entrySet().iterator();
        ArrayList<RegisteredHost> hosts = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, RegisteredHost> entry = (Map.Entry<String, RegisteredHost>) iterator.next();
            hosts.add(entry.getValue());
        }
        registeredHostService.batchStoreHosts(hosts);
    }

    public void addRegistrationExceptionList(ArrayList<RegistrationException> registrationExceptionList) {
        arrivalExceptions.addAll(registrationExceptionList);
    }

    private Date loadRegisteredHostsFromDB() {
        List<RegisteredHost> hosts = registeredHostService.getAllHosts();
        if(hosts==null || hosts.size()==0)
            return new Date();
        Date time = hosts.get(0).getUpdate_time();
        for (RegisteredHost rh : hosts) {
            registeredHosts.put(rh.getIp(), rh);
            if (rh.getId() != null)
                id2IpMap.put(rh.getId(), rh.getIp());
        }
        return time;
    }

    public void firstPublishIdentification() {
        latestRegisteredTime = loadRegisteredHostsFromDB();
        initiateHostInfos();
        if(registeredHosts.isEmpty())
            return;
        this.generateIdentifications();
        this.storePublishedHosts(registeredHosts);
        this.publishIdentifications(registeredHosts);
    }

    public void publishAdmitIdentification() {
        getNewArrivalHosts();
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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
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
            if (host.getId() != null)
                id2IpMap.put(host.getId(), host.getIp());
        }
        logger.info("publishIdentifications: finishes to publish host identification. registered host count={}", registeredHosts.size());
    }

    public void processExceptionIssues() {
        int batch_publish = 100;
        HashMap<String, RegisteredHost> newlyUpdatedHosts = new HashMap<>();
        Date time = new Date();
        RegisteredHost host = null;
        HostInfo info = null;
        Judgement judgement = new Judgement();
        while (!arrivalExceptions.isEmpty()) {
            RegistrationException registrationException = arrivalExceptions.peek();
            registrationExceptionService.storeException(registrationException);
            judgement.setIssueId(registrationException.getIssueId());
            judgement.setExceptionCode(PegaEnum.RegistrationExceptionCode.valueOf(registrationException.getCode()));
            judgement.setUpdateTime(time);
            switch (judgement.getExceptionCode()) {
                case NotFoundMatchedIp:
                    String[] ips = IdentityUtil.unpackNoMatchedIPException(registrationException.getIssueId());
                    for (int i = 0; i < ips.length; i++) {
                        info = hostInfoService.getHostInfo(ips[i]);
                        if (info == null)
                            continue;
                        host = registeredHosts.get(ips[i]);
                        if (host != null) {//if host exists, publish again
                            host.setUpdate_time(time);
                            newlyUpdatedHosts.put(ips[i], host);
                            logger.info("processExceptionIssues: NotFoundMatchedIp ip={} already registered, publish again", ips[i]);
                        } else {// if host not registered yet, create new registratedHostManagement and publish
                            host = hostInfo2RegisteredHost(info, time);
                            newlyUpdatedHosts.put(ips[i], host);
                            registeredHostService.storeHost(host);
                            logger.info("processExceptionIssues: NotFoundMatchedIp ip={} hasn't registered yet, first publish", ips[i]);
                        }
                        judgement.setStatus(PegaEnum.IssueStatus.finish);
                        judgement.setActionType(PegaEnum.ActionType.republish);
                        judgement.setContent(host.getIp());
                    }
                    if (info == null) {
                        judgement.setStatus(PegaEnum.IssueStatus.lasting);
                        judgement.setActionType(PegaEnum.ActionType.donothing);
                        judgement.setContent("cannot find valid ip from maintained hosts");
                        logger.info("processExceptionIssues: NotFoundMatchedIp ip={} hasn't fall under management yet, do nothing", registrationException.getIssueId());
                    }
                    break;
                case NotFoundUuid:
                    String ip = IdentityUtil.unpackNotFoundUuidException(registrationException.getIssueId());
                    if (ip == null) {//ip invalid,do nothing
                        logger.info("processExceptionIssues:NotFoundUuid cannot extract valid ip from issueid={}, exception lasting", registrationException.getIssueId());
                        judgement.setStatus(PegaEnum.IssueStatus.lasting);
                        judgement.setActionType(PegaEnum.ActionType.donothing);
                        judgement.setContent("cannot extract valid ip from issueid");
                        break;
                    }
                    host = registeredHosts.get(ip);
                    if (host == null) {
                        info = hostInfoService.getHostInfo(ip);
                        if (info == null) {//host info doesn't exist, do nothing
                            judgement.setStatus(PegaEnum.IssueStatus.lasting);
                            judgement.setActionType(PegaEnum.ActionType.donothing);
                            judgement.setContent("cannot find valid ip from maintained hosts");
                            logger.info("processExceptionIssues: NotFoundUuid ip={} hasn't fall under management yet, do nothing", registrationException.getIssueId());
                            break;
                        } else// create new registered host instance
                            host = this.hostInfo2RegisteredHost(info, time);
                    }
                    String allocatedUuid = UuidUtil.generateUuid(IdentityUtil.generateUuidInputString(host.getIp()));
                    host.setId(allocatedUuid);
                    host.setUpdate_time(time);
                    newlyUpdatedHosts.put(host.getIp(), host);
                    registeredHostService.storeHost(host);
                    judgement.setActionType(PegaEnum.ActionType.allocate);
                    judgement.setStatus(PegaEnum.IssueStatus.finish);
                    judgement.setContent(allocatedUuid);
                    logger.info("processExceptionIssues: NotFoundUuid ip={} has been allocated id={},exception resolved", host.getIp(), host.getId());
                    break;
                case UuidNotMatched:
                    ArrayList<String> phases = IdentityUtil.unpackUuidNotMatchedException(registrationException.getReason());
                    if (phases.size() != 4) {
                        judgement.setActionType(PegaEnum.ActionType.donothing);
                        judgement.setStatus(PegaEnum.IssueStatus.lasting);
                        judgement.setContent(phases.toString());
                        logger.info("processExceptionIssues: UuidNotMatched reason={} information is unqualified, exception lasting", registrationException.getReason());
                        break;
                    }
                    // host = registeredHostService.getHostById(registrationException.getReporter());
                    host = registeredHosts.get(id2IpMap.get(registrationException.getReporter()));
                    if (host == null) {//host with local identity doesn't exist in db
                        if (phases.contains(__PUBLISH)) {
                            host = registeredHostService.getHostById(phases.get(phases.indexOf(__PUBLISH) + 1));
                            if (host == null) {//host with published identity doesn't exist in db, do nothing
                                judgement.setActionType(PegaEnum.ActionType.donothing);
                                judgement.setStatus(PegaEnum.IssueStatus.lasting);
                                judgement.setContent("host with identity doesn't exist");
                                logger.info("processExceptionIssues: UuidNotMatched published id={}, reported id={} doesn't exist", registrationException.getReporter(), phases.get(phases.indexOf(__PUBLISH) + 1));
                                logger.warn("processExceptionIssues: UuidNotMatched reason={}. data inconsistency exists, request human intervention", registrationException.getReason());
                                break;
                            }
                            host.setUpdate_time(time);
                            newlyUpdatedHosts.put(host.getIp(), host);
                            registeredHostService.storeHost(host);
                            logger.info("processExceptionIssues: UuidNotMatched id={} has updated to new id={}, exception resolved", registrationException.getReporter(), host.getId());
                        }
                    } else {//host with local identity exists in db, update
                        if (phases.contains(__EXTRACT)) {//update to published extracted identity
                            String extractUuid = phases.get(phases.indexOf(__EXTRACT) + 1);
//                            registeredHostService.updateHostId(host.getId(),extractUuid);
//                            registeredHostService.updateUtime(extractUuid,time);
                            host.setId(extractUuid);
                            host.setUpdate_time(time);
                            registeredHostService.storeHost(host);
                            newlyUpdatedHosts.put(host.getIp(), host);
                            judgement.setActionType(PegaEnum.ActionType.extract);
                            judgement.setStatus(PegaEnum.IssueStatus.finish);
                            judgement.setContent(extractUuid);
                            logger.info("processExceptionIssues: UuidNotMatched change occurs in appealed entity. id={} will be updated to extracted id={}, exception resolved", registrationException.getReporter(), extractUuid);
                            break;
                        }
                        if (phases.contains(__PUBLISH)) {//update to published identity
                            String publishedUuid = phases.get(phases.indexOf(__PUBLISH) + 1);
//                            registeredHostService.updateHostId(host.getId(), publishedUuid);
//                            registeredHostService.updateUtime(publishedUuid,time);
                            host.setId(publishedUuid);
                            host.setUpdate_time(time);
                            registeredHostService.storeHost(host);
                            newlyUpdatedHosts.put(host.getIp(), host);
                            judgement.setStatus(PegaEnum.IssueStatus.finish);
                            judgement.setActionType(PegaEnum.ActionType.published);
                            judgement.setContent(host.getId());
                            logger.info("processExceptionIssues: UuidNotMatched id={} will be updated to published id={},exception resolved", registrationException.getReporter(), publishedUuid);
                            break;
                        }
                    }
                    break;
                case NameNotMatched:
                    judgement.setContent(registrationException.getReason());
                    judgement.setStatus(PegaEnum.IssueStatus.finish);
                    judgement.setActionType(PegaEnum.ActionType.donothing);
                    logger.info("processExceptionIssues: NameNotMatched id={} name update ={} is knowned and recoreded , do nothing", registrationException.getReporter(), registrationException.getReason());
                    break;
                default:
                    break;
            }
            arrivalExceptions.poll();
            judgementService.storeJudgement(judgement);
            exceptionStatus.put(judgement.getIssueId(), judgement.getStatus());
            if (newlyUpdatedHosts.size() == batch_publish) {
                this.publishIdentifications(newlyUpdatedHosts);
                registeredHosts.putAll(newlyUpdatedHosts);
                logger.info("processExceptionIssues: newly updated hosts has been published and stored, size={}", newlyUpdatedHosts.size());
                newlyUpdatedHosts.clear();
            }
        }
        if (newlyUpdatedHosts.size() != 0) {
            this.publishIdentifications(newlyUpdatedHosts);
            registeredHosts.putAll(newlyUpdatedHosts);
            logger.info("processExceptionIssues: newly updated hosts has been published and stored, size={}", newlyUpdatedHosts.size());
        }
    }

    //get newly updated hosts from host_info
    public void getNewlyUpdatedHosts() {

    }

    private RegisteredHost hostInfo2RegisteredHost(HostInfo info, Date time) {
        RegisteredHost host = new RegisteredHost();
        host.setUpdate_time(time);
        host.setIp(info.getIp());
        host.setId(null);
        String hostName = provinceUtil.getShortName(info.getHost_name().substring(0, 2));
        host.setHostName(IdentityUtil.generateRegisterName(pegaConfiguration.getWorkingNet(), hostName, host.getIp()));
        return host;
    }

    private void initiateHostInfos() {
        if (hostInfos != null)
            return;
        hostInfos = (ArrayList<HostInfo>) hostInfoService.getMaintainedHosts();
        lastReadHostInfos = new Date();
    }

    private void generateIdentifications() {
        logger.info("generateIdentifications: already has count={} host identifications", registeredHosts.size());
        String hostName;
        Date time = new Date();
        RegisteredHost host;
        for (HostInfo hostInfo : hostInfos) {
            host = registeredHosts.get(hostInfo.getIp());
            if (host != null)
                continue;
            host = new RegisteredHost();
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

    private void getNewArrivalHosts() {
        HashMap<String, ClaimNotice> claimNotices = claimNoticeManager.getLastRoundClaimNotices(latestRegisteredTime);
        if (claimNotices.size() == 0) {
            logger.info("getNewArrivalHosts: find no claim notice, do nothing ...");
            return;
        }
        logger.info("getNewArrivalHosts: find size={} new claim notice, start to process registratedHostManagement", claimNotices.size());
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
                logger.info("getNewArrivalHosts: register host ip={}, uuid={}", ip, host.getId());
            } else if (!host.getId().equals(claim.getValue().getUuid())) {
                logger.info("getNewArrivalHosts: find unequal id for host ip={}, register={},claim={},wait for exception process", ip, host.getId(), claim.getValue().getUuid());
            } else
                logger.info("getNewArrivalHosts: find equal id for host ip={},do nothing", ip, host.getId());
        }
        logger.info("getNewArrivalHosts: register size={} hosts successfully", newArrival.size());
    }
}
