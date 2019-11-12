package cert.aiops.pega.bean.mapping;

import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemMappings implements TabSerializable {
    private Logger logger = LoggerFactory.getLogger(SystemMappings.class);



    private HashMap<SystemRecorder, ArrayList<SystemSegment>> systemRecorderArrayListHashMap;

    public SystemMappings() {
//        systemMappingV = new HashMap<>();
        systemRecorderArrayListHashMap = new HashMap<>();
    }
    public HashMap<SystemRecorder, ArrayList<SystemSegment>> getMappings() {
        return systemRecorderArrayListHashMap;
    }

    @Override
    public String toTabbedString() {
        StringJoiner joiner = new StringJoiner(",");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<SystemRecorder, String> systemMappingAsString = new HashMap<>();
        try {
            for (Map.Entry<SystemRecorder, ArrayList<SystemSegment>> systemRecorderArrayListEntry : systemRecorderArrayListHashMap.entrySet()) {
                String jsonString = mapper.writeValueAsString(systemRecorderArrayListEntry.getValue());
                systemMappingAsString.put(systemRecorderArrayListEntry.getKey(), jsonString);
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return TabSerializer.objectsFromMapToList(systemMappingAsString, joiner);
    }

    public ArrayList<SystemSegment> getSystemMappingBySystemId(long systemId) {

        for (Map.Entry<SystemRecorder, ArrayList<SystemSegment>> systemRecorderArrayListEntry : systemRecorderArrayListHashMap.entrySet()) {

            SystemRecorder systemRecorder = systemRecorderArrayListEntry.getKey();
            if (systemRecorder.getId() == systemId)
                return systemRecorderArrayListEntry.getValue();
        }
        return null;
    }

    public ArrayList<SystemRecorder> getSystemRecorders() {
        ArrayList<SystemRecorder> recorders = new ArrayList<>();
        for (Map.Entry<SystemRecorder, ArrayList<SystemSegment>> systemRecorderArrayListEntry : systemRecorderArrayListHashMap.entrySet()) {
            SystemRecorder systemRecorder = systemRecorderArrayListEntry.getKey();
            recorders.add(systemRecorder);
        }
        return recorders;
    }

//    public SystemRecorder getSystemRecorderById(long systemId) {
//        Iterator iterator = systemRecorderArrayListHashMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            SystemRecorder systemRecorder = (SystemRecorder) entry.getKey();
//            if (systemRecorder.getId() == systemId)
//                return systemRecorder;
//        }
//        return null;
//    }

    public void deleteMappingBySystemId(long systemId) {
        ArrayList<SystemRecorder> recorders = new ArrayList<>();
        for (Map.Entry<SystemRecorder, ArrayList<SystemSegment>> systemRecorderArrayListEntry : systemRecorderArrayListHashMap.entrySet()) {
            SystemRecorder recorder = systemRecorderArrayListEntry.getKey();
            if (recorder.getId() == systemId)
                recorders.add(recorder);
        }
        for (SystemRecorder recorder : recorders)
            systemRecorderArrayListHashMap.remove(recorder);
    }

    public void updateSystemSegments(SystemRecorder systemRecorder, ArrayList<SystemSegment> segments) {
        deleteMappingBySystemId(systemRecorder.getId());
        addSystemMapping(systemRecorder, segments);
    }

    public void updateSystemSegments(long id, ArrayList<SystemSegment> segments) {
        SystemRecorder recorder = this.getRecorderById(id);
        deleteMappingBySystemId(id);
        assert recorder != null;
        addSystemMapping(recorder, segments);
    }

    public SystemRecorder getRecorderById(long id) {
        ArrayList<SystemRecorder> recorders = this.getSystemRecorders();
        for (SystemRecorder recorder : recorders)
            if (recorder.getId() == id)
                return recorder;
        return null;
    }

    public void addSystemMapping(SystemRecorder systemRecorder, ArrayList<SystemSegment> segments) {
        deleteMappingBySystemId(systemRecorder.getId());
        systemRecorderArrayListHashMap.put(systemRecorder, segments);
    }

    public void addSystemMapping(HashMap<SystemRecorder, ArrayList<SystemSegment>>  hashMap){
        for (Object o1 : systemRecorderArrayListHashMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o1;
            ArrayList<SystemSegment> segments = (ArrayList<SystemSegment>) entry.getValue();
            SystemRecorder recorder= (SystemRecorder) entry.getKey();
            addSystemMapping(recorder,segments);
        }
    }

    public ArrayList<SystemRecorder> getUnallocatedSystems() {
        ArrayList<SystemRecorder> systemRecorders = new ArrayList<>();
        for (Object o1 : systemRecorderArrayListHashMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o1;
            ArrayList<SystemSegment> segments = (ArrayList<SystemSegment>) entry.getValue();
            List<SystemSegment> temp=segments.stream().filter(o->o.getWorkerId()==null).collect(Collectors.toList());
            if (temp.size()!=0)
                systemRecorders.add((SystemRecorder) entry.getKey());
        }
        return systemRecorders;
    }

    public void updateUptime(Date time) {
        for (Map.Entry entry : systemRecorderArrayListHashMap.entrySet()) {
            SystemRecorder systemRecorder = (SystemRecorder) entry.getKey();
            systemRecorder.setUptime(time);
        }
    }

    public ArrayList<SystemSegment> filterSegementsById(long systemId, String workerId) {
        ArrayList<SystemSegment> segments = getSystemMappingBySystemId(systemId);
        ArrayList<SystemSegment> filterSegments = new ArrayList<>();
        for (SystemSegment segment : segments) {
            if (segment.getWorkerId() == null)
                continue;
            if (segment.getWorkerId().equals(workerId))
                filterSegments.add(segment);
        }
        return filterSegments;
    }
}
