package cert.aiops.pega.innerService;

import cert.aiops.pega.bean.mapping.WorkAssignment;
import cert.aiops.pega.bean.mapping.WorkerMappings;
import cert.aiops.pega.dao.WorkerMappingsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class WorkerMappingsService {

    @Autowired
    private WorkerMappingsDao dao;
    public void storeWorkerMappings(WorkerMappings mappings){
        dao.putWorkerMapping(mappings);
    }

    public ArrayList<WorkAssignment>  recoverFromDB(Date date,String workerId){
        return dao.getWorkerMappingsNearest(date,workerId);

    }
}
