package cert.aiops.pega.service;

import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.bean.SystemInfo;
import cert.aiops.pega.dao.HostInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Transactional
@Service
public class HostInfoService {
    @Autowired
    private HostInfoRepository hostInfoRepository;

    public SystemInfo initializeSystemInfoByName(String systemName){
        SystemInfo info = new SystemInfo();
        List<HostInfo> hosts = hostInfoRepository.findBySystem_name(systemName);
//        info.setHostCount(hosts.size());
        info.setHosts((ArrayList<HostInfo>) hosts);
        info.setSystemName(systemName);
        info.setId(hosts.get(0).getSystemId());
        info.setUpdateTime(hosts.get(hosts.size()-1).getUpdate_time());
        return info;
    }

    public  List<HostInfo>  getHostsBySystemName(String systemName){
        List<HostInfo> hosts = hostInfoRepository.findBySystem_name(systemName);
       return hosts;
    }

    public ArrayList<SystemInfo> getAllSystemInfos(){
        List<String> systems = hostInfoRepository.findDistinctBySystem_name();
        Iterator<String> sysIterator = systems.iterator();
        ArrayList<SystemInfo> sysList = new ArrayList<>();
        while(sysIterator.hasNext()){
            SystemInfo info = this.initializeSystemInfoByName(sysIterator.next());
            sysList.add(info);
        }
        return sysList;
    }

    public HostInfo getHostInfo(String ip){
        return hostInfoRepository.getByIp(ip);
    }
    public void deleteHostInfo(String ip){
        hostInfoRepository.deleteByIp(ip);
    }

    public void addHostInfo(HostInfo hostInfo){
//        hostInfoRepository.addHostInfo(hostInfo.getIp(),hostInfo.getHostName(),hostInfo.getNet(),hostInfo.getState(),hostInfo.getSystemId(),
//                hostInfo.getSystemId(),hostInfo.getUpdateTime());

        hostInfoRepository.save(hostInfo);
    }

    public void updateHostInfo(HostInfo hostInfo){
        hostInfoRepository.deleteByIp(hostInfo.getIp());
        this.addHostInfo(hostInfo);
    }

    public void updateSystemName(Long systemId, String newName){
        hostInfoRepository.updateSystemName(String.valueOf(systemId),newName);
    }

    public void addHostInfoList(ArrayList<HostInfo> hostList){
        List<HostInfo> data = new ArrayList<>();
        for(HostInfo info : hostList){
            if(data.size() == 400){
                hostInfoRepository.saveAll(data);
                data.clear();
            }
            data.add(info);
        }
        if(!data.isEmpty())
            hostInfoRepository.saveAll(data);
    }

    public List<HostInfo> getHostsBySystemIdAndIdRange(long systemId, long head, long tail){
        return hostInfoRepository.findBySystem_IdIPRange(systemId,head,tail);
    }

}
