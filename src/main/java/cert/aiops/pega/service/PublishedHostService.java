package cert.aiops.pega.service;

import cert.aiops.pega.bean.PublishedHost;
import cert.aiops.pega.dao.PublishedHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class PublishedHostService {
    @Autowired
    private PublishedHostRepository publishedHostRepository;

    public void batchStoreHosts(ArrayList<PublishedHost> hosts){
        publishedHostRepository.saveAll(hosts);
    }

    public List<PublishedHost> getAllHostByTime(Time beginning){
        return  publishedHostRepository.getAllBy(beginning);
    }

    public void updateId(String name,String id){
        publishedHostRepository.updateHostId(id,name);
    }

    public void updateHostName(String newName,String oldName){
        publishedHostRepository.updateHostName(newName,oldName);
    }

    public void updateChannels(String id,String channles){
        publishedHostRepository.updateChannels(id,channles);
    }

    public PublishedHost getHostByName(String hostName){
        return publishedHostRepository.getByHostName(hostName);
    }

    public PublishedHost getHostById(String id){
        return publishedHostRepository.getById(id);
    }
}
