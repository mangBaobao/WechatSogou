package cert.aiops.pega.innerService;

import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.bean.SystemInfo;
import cert.aiops.pega.dao.SystemInfoRepository;
import cert.aiops.pega.util.Sorter;
import cert.aiops.pega.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Transactional
@Service
public class SystemInfoService {
    Logger logger= LoggerFactory.getLogger(SystemInfoService.class);

    @Autowired
    private SystemInfoRepository systemInfoRepository;

    public SystemInfo initializeSystemInfoByName(String systemName) {
        SystemInfo systemInfo = systemInfoRepository.findBySystemName(systemName);
        HostInfoService hostInfoService = SpringContextUtil.getBean(HostInfoService.class);
        List<HostInfo> hosts = hostInfoService.getHostsBySystemName(systemName);
        systemInfo.setHosts((ArrayList<HostInfo>) hosts);
//        systemInfo.setHostCount(hosts.size());
        return systemInfo;
    }

    public List<SystemInfo>  getAllSystemNameAndId(){
        return systemInfoRepository.findAll();
    }
    public ArrayList<SystemInfo> getAllSystemInfos(){
        ArrayList<SystemInfo> sysList = new ArrayList<>();
        List<String> systems = systemInfoRepository.findDistinctBySystemName();
        logger.info("SystemInfoService_getAllSystemInfos: findDistinctBySystemName get system size={},including = {}",systems.size(),systems.toString());
        if(systems.size()==0){
            logger.info("SystemInfoService_getAllSystemInfos: find no system in repository,do nothing");
            return sysList;
        }
        Sorter<SystemInfo> sorter= new Sorter<>();
        Iterator<String> sysIterator = systems.iterator();
        while(sysIterator.hasNext()){
            SystemInfo info = this.initializeSystemInfoByName(sysIterator.next());
            sysList.add(info);
        }
            sorter.setCandidates(sysList);
            sysList= sorter.getSortedCandidates();
            return sysList;
    }

    public void addSystem(SystemInfo systemInfo){
        logger.info("SystemInfoService_addSystem: store system into repository,systemId={}",systemInfo.getId());
        systemInfoRepository.save(systemInfo);
    }

    public void deleteSystemByName(String systemName){
        systemInfoRepository.deleteBySystemName(systemName);
    }

    public void updateSystem(SystemInfo systemInfo){
        deleteSystemByName(systemInfo.getSystemName());
        addSystem(systemInfo);
    }

    public  SystemInfo getSystemInfoByName(String systemName){
        return systemInfoRepository.findBySystemName(systemName);
    }

    public  SystemInfo getSystemInfoById(long systemId){
        return systemInfoRepository.findBySystemId(systemId);
    }

    public void updateSystemName(String oldName, String newName){
        SystemInfo systemInfo = getSystemInfoByName(oldName);
        systemInfo.setSystemName(newName);
        systemInfoRepository.save(systemInfo);
    }

}
