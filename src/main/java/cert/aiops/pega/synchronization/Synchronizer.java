package cert.aiops.pega.synchronization;

import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.bean.SystemInfo;
import cert.aiops.pega.bean.SystemInfoClick;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.service.JczySynchronizationService;
import cert.aiops.pega.service.SystemInfoService;
import cert.aiops.pega.startup.BeingMasterCondition;
import cert.aiops.pega.util.Sorter;
import cert.aiops.pega.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@Conditional(value = {BeingMasterCondition.class})
public class Synchronizer {
    private Logger logger = LoggerFactory.getLogger(Synchronizer.class);

    private final String __SYSTERM = "system";
    private final String __DEVICE = "device";
    private final String __DEVTYPE = "服务器";
    private final String __BLADEDEV = "刀片服务器";

    @Autowired
    PegaConfiguration pegaConfiguration;
    //    @Autowired
//JczySynchronizationServiceImpl jczySynchronizationService;
    @Resource
    JczySynchronizationService jczySynchronizationService;

    /**
     * call jczy service to get all the Device and hosts .
     * and then initiate the m as pojo, and store them in database
     */

    public SystemInfo syncSystemByName(String state, String name) {
        Date time = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String uptime = format.format(time);
        List<JczySystemInfo> jczySystemInfo = jczySynchronizationService.getSystemByName(__SYSTERM, name);
        SystemInfo info = new SystemInfo();
        info.setSystemName(jczySystemInfo.get(0).getSname());
        info.setId(jczySystemInfo.get(0).getId());
        info.setIsmaintain(jczySystemInfo.get(0).getIsmaintain());
        info.setUpdateTime(uptime);
        ArrayList<SystemInfo> infos = new ArrayList<>();
        infos.add(info);
        infos = compareSystemInfo(infos);
        if (infos.size() != 0) {
            ArrayList<HostInfo> hostInfos = this.syncHostBySystemName(state, info.getSystemName(), uptime);
//        info.setHostCount(hostInfos.size());
            if(hostInfos!=null || hostInfos.size()!=0) {
                info.setHosts(hostInfos);
                logger.info("Synchronizer_syncSystemByName: system name={} is synchronized", name);
                return info;
            }
            else{
                logger.info("Synchronizer_syncSystemByName: system name={} is not in current working net,stop synchronization", name);
                return null;
            }
        } else {
            logger.info("Synchronizer_syncSystemByName: system name={} already in DB ,stop synchronization", name);
            return null;
        }
    }

    private String constructNetParameter(){
        String znet="涉密专网";
        if(pegaConfiguration.getWorkingNet().equals(znet))
            return "z";
        return "noNet";
    }

    private ArrayList<SystemInfo> compareSystemInfo(ArrayList<SystemInfo> infoFromSync) {
        SystemInfoService systemInfoService = SpringContextUtil.getBean(SystemInfoService.class);
        ArrayList<SystemInfo>   infoInDB = systemInfoService.getAllSystemInfos();
        ArrayList<SystemInfo> infoToDelete = new ArrayList<>();
        for (SystemInfo info : infoInDB) {
            long id = info.getId();
            for (SystemInfo infoSync : infoFromSync) {
                long idSync = infoSync.getId();
                if (idSync==id) {
                    infoToDelete.add(infoSync);
                    logger.info("Synchronizer_compareSystemInfo: system name ={} already in DB, stop synchronization", info.getSystemName());
                }
            }
        }
        infoFromSync.removeAll(infoToDelete);
        return infoFromSync;
    }

    public ArrayList<SystemInfo> syncSystemsByState(String state) {
        Date time = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String uptime = format.format(time);
        List<JczySystemInfo> jczySystemInfos = jczySynchronizationService.getSystemsByState(__SYSTERM, state);
        logger.info("Synchronizer_syncSystemByState:after jczySynchronizationService.getSystemsByState ({}), jczySystemInfos.size={}", state, jczySystemInfos.size());
        Iterator<JczySystemInfo> jczySystemInfoIterator = jczySystemInfos.iterator();
        ArrayList<SystemInfo> systemInfos = new ArrayList<>();
        while (jczySystemInfoIterator.hasNext()) {
            JczySystemInfo jczySystemInfo = jczySystemInfoIterator.next();
            SystemInfo info = new SystemInfo();
            info.setSystemName(jczySystemInfo.getSname());
            info.setId(jczySystemInfo.getId());
            info.setIsmaintain(jczySystemInfo.getIsmaintain());
            info.setUpdateTime(uptime);
            systemInfos.add(info);
        }
//        systemInfos = compareSystemInfo(systemInfos);
        compareSystemInfo(systemInfos);
        if(systemInfos.size()==0){
            logger.info("Synchronizer_syncSystemsByState:all the systems are already synchronized. Stop synchronization");
            return systemInfos;
        }
        else{
            logger.info("Synchronizer_syncSystemsByState:after compareSystemInfo, systemInfo size={},systems={}",systemInfos.size(),systemInfos.toString());
        }
        Sorter<SystemInfo> sorter = new Sorter<>();
        sorter.setCandidates(systemInfos);
        systemInfos = sorter.getSortedCandidates();

        ArrayList<SystemInfo> systemsNotInNet=new ArrayList<>();
        for (SystemInfo s : systemInfos) {
            ArrayList<HostInfo> hostInfos = this.syncHostBySystemName(state, s.getSystemName(), uptime);
            if(hostInfos.size()!=0) {
                s.setHosts(hostInfos);
                logger.info("Synchronizer_syncSystemsByState: system name={} is synchronized", s.getSystemName());
            }
            else{
                systemsNotInNet.add(s);
                logger.info("Synchronizer_syncSystemsByState:find system name ={} don't have hosts in current working net",s.getSystemName());
            }
        }
        systemInfos.removeAll(systemsNotInNet);
        return systemInfos;
    }

    private ArrayList<HostInfo> syncHostBySystemName(String state, String system, String uptime) {
        ArrayList<HostInfo> hostInfos = new ArrayList<>();
        List<JczyDeviceInfo> deviceInfos = jczySynchronizationService.getHostsBySystemAndNet(__DEVICE, constructNetParameter(), system, __DEVTYPE);
        if (deviceInfos.size() != 0 || deviceInfos!=null)
            hostInfos.addAll(turnJczyDevice2Host(state, uptime, deviceInfos));
        deviceInfos = jczySynchronizationService.getHostsBySystemAndNet(__DEVICE, constructNetParameter(),system, __BLADEDEV);
        if (deviceInfos.size() != 0 || deviceInfos!=null)
            hostInfos.addAll(turnJczyDevice2Host(state, uptime, deviceInfos));
//        Iterator<JczyDeviceInfo> deviceInfoIterator = deviceInfos.iterator();
//        ArrayList<HostInfo> hostInfos = new ArrayList<>();
//        String workingNet=pegaConfiguration.getWorkingNet();
//        while(deviceInfoIterator.hasNext()){
//         JczyDeviceInfo deviceInfo = deviceInfoIterator.next();
//         if(deviceInfo.getBnetwork_name()==null)
//             continue;
//         if(!deviceInfo.getBnetwork_name().equals(workingNet))
//             continue;
//         if(!deviceInfo.getDstatus_name().equals(state))
//             continue;
//            HostInfo hostInfo = new HostInfo();
//            hostInfo.setSn(deviceInfo.getDsn());
//            hostInfo.setSystemId(deviceInfo.getSid());
//            hostInfo.setState(PegaEnum.State.valueOf(deviceInfo.getDstatus_name()));
//            hostInfo.setSystem_name(deviceInfo.getSname());
//            hostInfo.setId(Long.valueOf(deviceInfo.getDid()));
//            hostInfo.setHostName(deviceInfo.getDname());
//            hostInfo.setUpdateTime(uptime);
////            hostInfo.setNet(PegaEnum.Net.valueOf(deviceInfo.getBnetwork_name()));
//            hostInfo.setIp(deviceInfo.getIp());
//            hostInfos.add(hostInfo);
//        }
        Sorter<HostInfo> sorter = new Sorter<>();
        sorter.setCandidates(hostInfos);
        logger.info("Synchronizer_syncSystemsByState: hosts count={} of system name={} in state={} are synchronized", hostInfos.size(), system, state);
        return sorter.getSortedCandidates();
    }

    private ArrayList<HostInfo> turnJczyDevice2Host(String state, String uptime, List<JczyDeviceInfo> deviceInfos) {
        Iterator<JczyDeviceInfo> deviceInfoIterator = deviceInfos.iterator();
        ArrayList<HostInfo> hostInfos = new ArrayList<>();
        String workingNet = pegaConfiguration.getWorkingNet();
        while (deviceInfoIterator.hasNext()) {
            JczyDeviceInfo deviceInfo = deviceInfoIterator.next();
            if (deviceInfo.getBnetwork_name() == null) {
                logger.info("turnJczyDevice2Host:ignored device for network_name;info={}", deviceInfo.toString());
                continue;
            }
            if (!deviceInfo.getBnetwork_name().equals(workingNet)) {
                logger.info("turnJczyDevice2Host:ignored device for working net; info={}", deviceInfo.toString());
                continue;
            }
            if (!deviceInfo.getDstatus_name().equals(state)) {
                logger.info("turnJczyDevice2Host:ignored device for state;  info={}", deviceInfo.toString());
                continue;
            }
            HostInfo hostInfo = new HostInfo();
            hostInfo.setSn(deviceInfo.getDsn());
            hostInfo.setSystemId(deviceInfo.getSid());
            hostInfo.setState(PegaEnum.State.valueOf(deviceInfo.getDstatus_name()));
            hostInfo.setSystem_name(deviceInfo.getSname());
            hostInfo.setId(Long.valueOf(deviceInfo.getDid()));
            hostInfo.setHost_name(deviceInfo.getDname());
            hostInfo.setUpdate_time(uptime);
//            hostInfo.setNet(PegaEnum.Net.valueOf(deviceInfo.getBnetwork_name()));
            hostInfo.setIp(deviceInfo.getIp());

            hostInfos.add(hostInfo);
        }
        return hostInfos;
    }
    /*
    todo : if system add new hosts, tag them in segment with workerId=null; if remove all the system,tag sytemRecord as invalid;
    if remove some hosts,tag hostinfo state as 失效
     */

    public ArrayList<SystemInfoClick> incSyncWithJczy() {
        return null;
    }


}
