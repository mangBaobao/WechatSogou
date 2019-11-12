package cert.aiops.pega.bean.mapping;

import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class WorkerMappings implements TabSerializable {
    private Logger logger = LoggerFactory.getLogger(WorkerMappings.class);
    private HashMap<WorkerRecorder, ArrayList<WorkAssignment>> workerRecorderArrayListHashMap;
//    private HashMap<WorkerRecorder, ArrayList<WorkAssignment>> mappingInV;

    public WorkerMappings() {
//        mappingInV = new HashMap<>();
        workerRecorderArrayListHashMap = new HashMap<>();
    }

    @Override
    public String toTabbedString() {
        StringJoiner joiner;
        joiner = new StringJoiner(",");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<WorkerRecorder, String> mappingAsString = new HashMap<>();
        try {
//            iterator = mappingInV.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry entry = (Map.Entry) iterator.next();
//                String json = mapper.writeValueAsString(entry.getValue());
//                mappingAsString.put((WorkerRecorder) entry.getKey(), json);
//            }
            for (Object o : workerRecorderArrayListHashMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String json = mapper.writeValueAsString(entry.getValue());
                mappingAsString.put((WorkerRecorder) entry.getKey(), json);
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return TabSerializer.objectsFromMapToList(mappingAsString, joiner);
    }

    @Override
    public String toString() {
        return "WorkerMappings{" +
                "workerRecorderArrayListHashMap=" + workerRecorderArrayListHashMap +
                '}';
    }

    public HashMap<WorkerRecorder, ArrayList<WorkAssignment>> getMappings() {
        return workerRecorderArrayListHashMap;
    }


    public void addWorkerMapping(WorkerRecorder worker, ArrayList<WorkAssignment> ips) {
        this.deleteMapping(worker);
        workerRecorderArrayListHashMap.put(worker, ips);
    }

    public int getSize(){
        return workerRecorderArrayListHashMap.size();
    }

    public void deleteMapping(WorkerRecorder worker) {
        ArrayList<WorkerRecorder> recorders=new ArrayList<>();
        for (Object o : workerRecorderArrayListHashMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            WorkerRecorder workerRecorder = (WorkerRecorder) entry.getKey();
            if (workerRecorder.getId().equals(worker.getId())) {
               recorders.add(workerRecorder);
            }
        }
        for(WorkerRecorder recorder:recorders)
            workerRecorderArrayListHashMap.remove(recorder);
    }

    public Date getRecordTime() {
        for (Map.Entry<WorkerRecorder, ArrayList<WorkAssignment>> workerRecorderArrayListEntry : workerRecorderArrayListHashMap.entrySet()) {
            WorkerRecorder bean = workerRecorderArrayListEntry.getKey();
            return bean.getRecordTime();
        }
        return null;
    }

    public ArrayList<WorkAssignment> getMappingByWorkerId(String workerId) {

        for (Map.Entry<WorkerRecorder, ArrayList<WorkAssignment>> workerRecorderArrayListEntry : workerRecorderArrayListHashMap.entrySet()) {
            WorkerRecorder bean = workerRecorderArrayListEntry.getKey();
            if (bean.getId().equals(workerId))
                return workerRecorderArrayListEntry.getValue();
        }
        return null;
    }

    public ArrayList<WorkAssignment> getMappingByWorker(WorkerRecorder worker){

        if(!workerRecorderArrayListHashMap.containsKey(worker))
            return null;
        return workerRecorderArrayListHashMap.get(worker);
    }

    public void updateRecordTime(Date time){
        for(Map.Entry entry:workerRecorderArrayListHashMap.entrySet()){
            WorkerRecorder recorder= (WorkerRecorder) entry.getKey();
            recorder.setRecordTime(time);
        }
    }

    public void updateMapping(WorkerRecorder worker, ArrayList<WorkAssignment> assignments) {
        this.deleteMapping(worker);
        this.addWorkerMapping(worker, assignments);
    }
}
