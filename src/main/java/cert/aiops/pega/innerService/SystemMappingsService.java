package cert.aiops.pega.innerService;

import cert.aiops.pega.bean.mapping.SystemMappings;
import cert.aiops.pega.bean.mapping.SystemRecorder;
import cert.aiops.pega.bean.mapping.SystemSegment;
import cert.aiops.pega.dao.SystemMappingsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class SystemMappingsService {

    @Autowired
    SystemMappingsDao dao;

    public void storeSystemMappings(SystemMappings mappings){
        dao.putSystemMappings(mappings);
    }

    public HashMap<SystemRecorder, ArrayList<SystemSegment>> recoverFromDB(Date date,String workerId){
       return  dao.getSystemMappingsLaterThan(date,workerId);
    }
}
