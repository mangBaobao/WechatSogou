package cert.aiops.pega.service;

import cert.aiops.pega.bean.RegisteredHost;
import cert.aiops.pega.dao.RegisteredHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class RegisteredHostService {
    @Autowired
    private RegisteredHostRepository registeredHostRepository;

    public void batchStoreHosts(ArrayList<RegisteredHost> hosts){
        registeredHostRepository.saveAll(hosts);
    }

    public void storeHost(RegisteredHost host){
        registeredHostRepository.save(host);
    }

    public List<RegisteredHost> getAllHostByTime(Time beginning){
        return  registeredHostRepository.getAllBy(beginning);
    }

    public List<RegisteredHost> getAllHosts(){
        return registeredHostRepository.getAllHosts();
    }

    public void updateId(String name,String id){
        registeredHostRepository.updateHostIdByName(id,name);
    }

    public void updateHostName(String newName,String oldName){
        registeredHostRepository.updateHostName(newName,oldName);
    }

    public RegisteredHost getLatestAdmitHost(){
        return registeredHostRepository.getLatestAdmitHost();
    }

    public void updateChannels(String id,String channles){
        registeredHostRepository.updateChannels(id,channles);
    }

    public RegisteredHost getHostByName(String hostName){
        return registeredHostRepository.getByHostName(hostName);
    }

    public RegisteredHost getHostById(String id){
        return registeredHostRepository.getById(id);
    }

    public void updateHostId(String newId,String oldId){
        registeredHostRepository.updateHostIdById(newId,oldId);
    }

    public void updateUtime(String id, Date time){

    }
}
