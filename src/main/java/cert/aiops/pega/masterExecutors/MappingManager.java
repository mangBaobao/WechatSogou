package cert.aiops.pega.masterExecutors;

import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.bean.SystemInfo;
import cert.aiops.pega.bean.mapping.*;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.service.SystemMappingsService;
import cert.aiops.pega.service.WorkerMappingsService;
import cert.aiops.pega.startup.BeingMasterCondition;
import cert.aiops.pega.util.Sorter;
import cert.aiops.pega.util.ZookeeperUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Conditional(value = {BeingMasterCondition.class})
public class MappingManager {
    private Logger logger = LoggerFactory.getLogger(MappingManager.class);
    private WorkerMappings workerMappings;
    private SystemMappings systemMappings;

    @Autowired
    PegaConfiguration pegaConfiguration;
    @Autowired
    WorkerMappingsService workerMappingsService;
    @Autowired
    SystemMappingsService systemMappingsService;

    private ArrayList<SystemInfo> systemInfos;

    private ArrayList<WorkerRecorder> workerRecorders;


    public MappingManager() {

        workerRecorders = new ArrayList<>();
        workerMappings = new WorkerMappings();
        systemMappings = new SystemMappings();
    }


    void initAllMappings() {
        checkWorkersState();
        ArrayList<SystemInfo> uninitiateSystemInfos = findUninitiateSystemInfos();
        initSystemRecorders(uninitiateSystemInfos, this.systemMappings);
        if (workerRecorders.size() == 0) {
            logger.info("initAllMappings: workerRecorders=0, do nothing");
            return;
        }
        Date time = new Date();
        List<String> children = ZookeeperUtil.getInstance().getChildren(pegaConfiguration.getMappingPath());
        if (children.size() != 0) {//if zk tree keeps mapping information
            logger.info("initAllMappings: load workerMappings from ZKTree");
            loadWorkerMappingsFromZKTree();
            logger.info("initAllMappings: load systemMappings from ZKTree");
            loadSystemMappingsFromZKTree();
            logger.info("initAllMappings_after load from ZKTree,workerMappings={}", workerMappings.toTabbedString());
            logger.info("initAllMappings_after load from ZKTree,systemMappings={}", systemMappings.toTabbedString());
        }
        if (workerMappings.getSize() == 0) {
            for (WorkerRecorder workerRecorder : workerRecorders) {
                workerMappings.addWorkerMapping(workerRecorder, workerMappingsService.recoverFromDB(new Date(), workerRecorder.getId()));
                //if (workerMappings != null) {//if db keeps mappings

                systemMappings.addSystemMapping(systemMappingsService.recoverFromDB(workerMappings.getRecordTime(), workerRecorder.getId()));
                //    }
            }
            logger.info("initAllMappings_after load from DB,workerMappings' size={}", workerMappings.getSize());
        }
        //  if (systemMappings == null || systemMappings.getSystemRecorders().size() == 0)
        uninitiateSystemInfos = findUninitiateSystemInfos();
        initSystemRecorders(uninitiateSystemInfos, this.systemMappings);
        updateWorkerRecorders();
        updateSystemRecorders();
        allocateSystemToWorkers(systemMappings, workerMappings);
        logger.info("initAllMappings_after allocateSystemToWorkers,workerMappings={}", workerMappings.toTabbedString());
        logger.info("initAllMappings_after allocateSystemToWorkers,systemMappings={}", systemMappings.toTabbedString());
        writeMappingsToDB(time);
        writeWorkerMappingsToZKTree();
        writeSystemMappingsToZKTree();
    }

    private ArrayList<SystemInfo> findUninitiateSystemInfos() {
        String systemPath = pegaConfiguration.getSystemPath();
        ArrayList<SystemInfo> unmappingSystems = new ArrayList<>();
        for (SystemInfo info : systemInfos) {
            String systemId = String.valueOf(info.getId());
            String value = ZookeeperUtil.getInstance().getData(ZookeeperUtil.getInstance().concatPath(systemPath, systemId));
            if (value == null || value.equals("")) {
                unmappingSystems.add(info);
                logger.info("MappingManager_findUnmappingSystemInfos:find an unmapped systeminfo, name={}", info.getSystemName());
            }
        }
        return unmappingSystems;

    }

    int getWorkerSize() {
        return workerRecorders.size();
    }

    String getWorkerIds() {
        StringJoiner joiner = new StringJoiner(",");
        for (WorkerRecorder recorder : workerRecorders) {
            joiner.add(recorder.getId());
        }
        return joiner.toString();
    }

    boolean updateSystemRecorders() {
        boolean isChanged = false;
//        if (workerRecorders.size() == 0) {
//            logger.info("updateSystemRecorders: worker Recorder size=0.do nothing");
//            return isModified;
//        }
        ArrayList<SystemRecorder> systemRecorders = systemMappings.getSystemRecorders();
        String systemPath = pegaConfiguration.getSystemPath();
        for (SystemRecorder recorder : systemRecorders) {
            if (recorder.getState() == PegaEnum.ObjectState.invalid) {
                for (WorkerRecorder workerRecorder : workerRecorders) {
                    ArrayList<WorkAssignment> assignments = (ArrayList<WorkAssignment>) workerMappings.getMappingByWorker(workerRecorder).stream()
                            .filter(o -> o.getSystemId() == recorder.getId()).collect(Collectors.toList());
                    if (assignments == null || assignments.size() == 0)
                        continue;
                    int count = assignments.stream().mapToInt(WorkAssignment::getCount).sum();
                    workerRecorder.setMonitorCount(workerRecorder.getMonitorCount() - count);
                    ArrayList<WorkAssignment> assignmentNew = workerMappings.getMappingByWorkerId(workerRecorder.getId());
                    assignmentNew.removeAll(assignments);
                    workerMappings.updateMapping(workerRecorder, assignmentNew);
                }
                logger.info("updateSystemRecorders:begins to clear and remove invalid systemNode on ZK tree, ID={}", recorder.getId());
                ZookeeperUtil.getInstance().deleteNode(ZookeeperUtil.getInstance().concatPath(systemPath, String.valueOf(recorder.getId())));
                logger.info("updateSystemRecorders:begins to clear and remove invalid systemRecorder, ID={}", recorder.getId());
                systemMappings.deleteMappingBySystemId(recorder.getId());
                logger.info("updateSystemRecorders:finishes clearing and removing invalid systemRecorder, ID={}", recorder.getId());
                isChanged = true;
            }
        }

        updateSystemInfos();

        for (SystemRecorder recorder : systemRecorders) {
            ArrayList<SystemSegment> segments = systemMappings.getSystemMappingBySystemId(recorder.getId());
            for (SystemSegment segment : segments) {
                if (segment.getWorkerId() == null) //indicate this segment is not allocated yet
                    continue;
                if (this.getWorkerRecorderById(segment.getWorkerId()) == null) { // indicate worker recorder has been changed and not yet reflected
                    logger.info("updateSystemRecorders:find a out-of-date system segment, worker id={},header={},trailer={}", segment.getWorkerId(),
                            segment.getHeader(),segment.getTrailer());
                    segment.setWorkerId(null);
                    logger.info("updateSystemRecorders:recall system segment ");
                    isChanged = true;
                }
            }
        }

        return isChanged;
    }

    void updateSystemInfos(){
        ArrayList<SystemInfo> deletedInfos=new ArrayList<>();
        for(SystemInfo info:systemInfos){
            if(info.getIsmaintain()!= PegaEnum.State.在维) {
                logger.info("updateSystemInfos: find an out-of-date systemInfo, ID={}, name={}", info.getId(),info.getSystemName());
                deletedInfos.add(info);
            }
        }
        logger.info("updateSystemInfos: begins to clear and remove out-of-date systemInfos, count={}", deletedInfos.size());
         deletedInfos.removeAll(deletedInfos);
    }

    boolean checkAllocationFragment() {
        int systemCount = systemInfos.size();
        int workermappingCountInTotal = 0;
        double fragThreshold = pegaConfiguration.getFragFactor();
        String wokermappingCountAsString = "";
        for (WorkerRecorder worker : workerRecorders) {
            int workerMappingCount = workerMappings.getMappingByWorkerId(worker.getId()).size();
            workermappingCountInTotal += workerMappingCount;
            wokermappingCountAsString += worker.getId() + ":" + String.valueOf(workerMappingCount) + ",";
        }
        logger.info("checkAllocationFragment: worker mapping counts:{}", wokermappingCountAsString);

        double fragScore = ((double) workermappingCountInTotal) / systemCount;

        logger.info("checkAllocationFragment: calculated fragment score={},with workermappingCountInTotal={},systemCount={}", fragScore, workermappingCountInTotal, systemCount);

        if (fragScore >= fragThreshold) {
            logger.info("checkAllocationFragement: final fragScore={},fragmentThreshold={}. Remapping is required", fragScore, fragThreshold);
            return true;
        } else {
            logger.info("checkAllocationFragement: final fragScore={},fragmentThreshold={}. Remapping is unnecessary", fragScore, fragThreshold);
            return false;
        }
    }

    void remappingAllSystems() {
        logger.info("remappingAllSystems is about to begins:create new systemMappings, workerMappings ");
        SystemMappings systemMappings = new SystemMappings();
        WorkerMappings workerMappings = new WorkerMappings();
        initSystemRecorders(systemInfos, systemMappings);
        for (WorkerRecorder recorder : workerRecorders) {
            recorder.setMonitorCount(0);
            ArrayList<WorkAssignment> ips = new ArrayList<>();
            workerMappings.addWorkerMapping(recorder, ips);
        }
        logger.info("remappingAllSystems_ initSystemRecorders: all the system infos ");
        allocateSystemToWorkers(systemMappings, workerMappings);
        this.systemMappings = systemMappings;
        this.workerMappings = workerMappings;
        logger.info("remappingAllSystems_after allocateSystemToWorkers,workerMappings={}", workerMappings.toTabbedString());
        logger.info("remappingAllSystems_after allocateSystemToWorkers,systemMappings={}", systemMappings.toTabbedString());
        writeMappingsToDB(new Date());
        writeWorkerMappingsToZKTree();
        writeSystemMappingsToZKTree();
        logger.info("remappingAllSystems ends ");
    }

    SystemMappings getSystemMappings() {
        return this.systemMappings;
    }

    boolean checkWorkersState() {
        List<String> workerIds = ZookeeperUtil.getInstance().getChildren(pegaConfiguration.getWorkerPath());
        boolean hasNewWorker = false;
        for (WorkerRecorder workerRecorder : workerRecorders) {
            String workerId = workerRecorder.getId();
            if (workerIds.contains(workerId)) {
                workerIds.remove(workerId);
                workerRecorder.setState(PegaEnum.ObjectState.valid);
            } else {
                logger.info("checkWorkerState: found invalid worker, ID={}", workerId);
                workerRecorder.setState(PegaEnum.ObjectState.invalid);
            }
        }
        if (workerIds.size() != 0) {
            hasNewWorker = true;
            for (String workerId : workerIds) {
                logger.info("checkWorkerState: found new worker,ID={}", workerId);
                WorkerRecorder workerRecorder = new WorkerRecorder();
                workerRecorder.setState(PegaEnum.ObjectState.valid);
                workerRecorder.setId(workerId);
                workerRecorder.setMonitorCount(0);
                this.addWorker(workerRecorder);
            }
        }
        return hasNewWorker;
    }

    private WorkerRecorder getWorkerRecorderById(String id) {
        for (WorkerRecorder workerRecorder : workerRecorders) {
            if (workerRecorder.getId().equals(id))
                return workerRecorder;
        }
        return null;
    }

    boolean updateWorkerRecorders() {

        boolean isModified = false;
        ArrayList<SystemRecorder> systemRecorders = systemMappings.getSystemRecorders();
//        if (systemRecorders.size() == 0) {
//            logger.info("updateWorkerRecorders: no system recorder found. do nothing");
//            return isCleared;
//        }
        ArrayList<WorkerRecorder> workersToCleared = new ArrayList<>();
        String mappingPath = pegaConfiguration.getMappingPath();
        for (WorkerRecorder workerRecorder : workerRecorders) {
            if (workerRecorder.getState() == PegaEnum.ObjectState.invalid) {
                for (SystemRecorder recorder : systemRecorders) {
                    ArrayList<SystemSegment> segments = systemMappings.getSystemMappingBySystemId(recorder.getId());
                    List<SystemSegment> segmentsFiltered = systemMappings.filterSegementsById(recorder.getId(), workerRecorder.getId());
                    if (segmentsFiltered.size() == 0)
                        continue;
                    int count = segmentsFiltered.stream().mapToInt(SystemSegment::getCount).sum();
                    recorder.setAllocatedCount(recorder.getAllocatedCount() - count);
                    recorder.setUnallocatedCount(recorder.getUnallocatedCount() + count);
                    for (int i = 0; i < segmentsFiltered.size(); i++) {
                        segmentsFiltered.get(i).setWorkerId(null);
                    }

                    logger.info("updateWorkerRecorders:concerned system id={}, updated segments={}", recorder.getId(), segmentsFiltered.toString());
                    systemMappings.updateSystemSegments(recorder, segments);
                }
                ZookeeperUtil.getInstance().deleteNode(ZookeeperUtil.getInstance().concatPath(mappingPath, workerRecorder.getId()));
                logger.info("updateWorkerRecorders:add invalid workerRecord to clearList, ID={}", workerRecorder.getId());
                workersToCleared.add(workerRecorder);
                isModified = true;
            }
        }

        for (WorkerRecorder workerRecorder : workersToCleared) {
            logger.info("updateWorkerRecorders:begins to clear and remove invalid workerRecord, ID={}", workerRecorder.getId());
            workerRecorders.remove(workerRecorder);
            workerMappings.deleteMapping(workerRecorder);
            logger.info("updateWorkerRecorders:finishes  clearing and removing invalid workerRecord, ID={}", workerRecorder.getId());
        }

        for (WorkerRecorder workerRecorder : workerRecorders) {
            String path = ZookeeperUtil.getInstance().concatPath(mappingPath, workerRecorder.getId());
            boolean isExists = ZookeeperUtil.getInstance().checkExists(path);
            logger.info("MappingManager_checkWorkerRecorders_forInconsitencyCheck:path={},isExists={}", path, isExists);
            if (isExists) {
                String content = ZookeeperUtil.getInstance().getData(path);
                if (content.equals("[]"))
                    isModified = true;
            }
        }
        return isModified;
    }

    private void initSystemRecorders(ArrayList<SystemInfo> systemInfos, SystemMappings systemMappings) {
        logger.info("MappingManager_initSystemRecorders:begins to initSystemRecorders");
//        systemMappings=systemMappingsService.recoverFromDB(new Date());
//        if(systemMappings.getSystemRecorders()!=null)
//            return;
        for (SystemInfo systemInfo : systemInfos) {
            ArrayList<HostInfo> hostInfos = systemInfo.getHosts();
            if (hostInfos != null) {
                SystemRecorder bean = new SystemRecorder();
                bean.setId(systemInfo.getId());
                bean.setAllocatedCount(0);
                bean.setState(PegaEnum.ObjectState.valid);
                bean.setUnallocatedCount(hostInfos.size());
                ArrayList<SystemSegment> segments = new ArrayList<>();
                SystemSegment segment = new SystemSegment();
                segment.setHeader(hostInfos.get(0).getId());
                segment.setTrailer(hostInfos.get(hostInfos.size() - 1).getId());
                segment.setCount(systemInfo.getHostCount());
                segment.setWorkerId(null);
                segments.add(segment);
                systemMappings.addSystemMapping(bean, segments);
            }
        }
        logger.info("MappingManager_initSystemRecorders: after initSystemRecorders,systemMappings={}", systemMappings.toTabbedString());
        writeSystemMappingsToZKTree();
    }

    void writeMappingsToDB(Date time) {
        workerMappings.updateRecordTime(time);
        workerMappingsService.storeWorkerMappings(workerMappings);
        systemMappings.updateUptime(time);
        systemMappingsService.storeSystemMappings(systemMappings);
    }

    void writeWorkerMappingsToZKTree() {
        String mappingPath = pegaConfiguration.getMappingPath();
        Iterator iterator = workerMappings.getMappings().entrySet().iterator();
        ObjectMapper mapper = new ObjectMapper();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            WorkerRecorder workerRecorder = (WorkerRecorder) entry.getKey();
            ArrayList<WorkAssignment> assignments = (ArrayList<WorkAssignment>) entry.getValue();
            String nodePath = ZookeeperUtil.getInstance().concatPath(mappingPath, workerRecorder.getId());
            try {
                logger.info("writeWorkerMappingsToZKTree: worker Id={},nodePath={},assignments={}", workerRecorder.getId(), nodePath, mapper.writeValueAsString(assignments));
//                boolean ifExists = ZookeeperUtil.getInstance().checkExists(nodePath);
//                if (ifExists == false)
                ZookeeperUtil.getInstance().deleteNode(nodePath);
                ZookeeperUtil.getInstance().createPersistenceNode(nodePath, mapper.writeValueAsString(assignments));
//                else
//                    ZookeeperUtil.getInstance().setData(nodePath, mapper.writeValueAsString(assignments));
            } catch (JsonProcessingException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    void writeSystemMappingsToZKTree() {
        String mappingPath = pegaConfiguration.getSystemPath();
        Iterator iterator = systemMappings.getMappings().entrySet().iterator();
        ObjectMapper mapper = new ObjectMapper();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SystemRecorder recorder = (SystemRecorder) entry.getKey();
            ArrayList<SystemSegment> segments = (ArrayList<SystemSegment>) entry.getValue();
            String nodePath = ZookeeperUtil.getInstance().concatPath(mappingPath, String.valueOf(recorder.getId()));
            try {
                logger.info("writeSystemMappingsToZKTree: system Id={},nodePath={},segments={}", recorder.getId(), nodePath, mapper.writeValueAsString(segments));
                boolean ifExists = ZookeeperUtil.getInstance().checkExists(nodePath);
                if (ifExists)
                    ZookeeperUtil.getInstance().deleteNode(nodePath);
                ZookeeperUtil.getInstance().createPersistenceNode(nodePath, mapper.writeValueAsString(segments));
//                ZookeeperUtil.getInstance().setData(nodePath,mapper.writeValueAsString(segments));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isAllTheSystemsAllocated() {
        int monitorCountInTotal = 0;
        for (WorkerRecorder recorder : workerRecorders)
            monitorCountInTotal += recorder.getMonitorCount();
        int hostCountInTotal = 0;
        for (SystemInfo info : systemInfos)
            hostCountInTotal += info.getHostCount();
        if (hostCountInTotal != monitorCountInTotal) {
            logger.info("isAllTheSystemsAllocated: monitorCountInTotal={}, hostCountInTotal={}. Remapping is required",monitorCountInTotal,hostCountInTotal);
            return false;
        } else {
            logger.info("isAllTheSystemsAllocated: monitorCountInTotal={}, hostCountInTotal={}. Remapping is unnecessary",monitorCountInTotal,hostCountInTotal);
            return true;
        }
    }

    boolean checkSystemState(){
        boolean isChanged=false;
        for(SystemInfo info: systemInfos){
            if(info.getIsmaintain()!= PegaEnum.State.在维){
                isChanged=true;
                SystemRecorder recorder=systemMappings.getRecorderById(info.getId());
                recorder.setState(PegaEnum.ObjectState.invalid);
            }
        }
        ArrayList<SystemInfo>  newSystems=findUninitiateSystemInfos();
        if(newSystems.size()!=0)
            isChanged=true;

        return isChanged;
    }

    void allocateSystemToWorkers(SystemMappings systemMappings, WorkerMappings workerMappings) {
        int monitorCountThredshold = pegaConfiguration.getMonitorCountThredshold();
        ArrayList<SystemRecorder> systemRecorders = systemMappings.getUnallocatedSystems();
        if (systemRecorders.size() == 0) {
            logger.info("allocateSystemToWorkers: no unallocated systems available.do nothing");
            return;
        }
        Sorter<SystemRecorder> sorter = new Sorter<>();
        sorter.setCandidates(systemRecorders);
        systemRecorders = sorter.getSortedCandidates();
        Sorter<WorkerRecorder> sorterW = new Sorter<>();
        sorterW.setCandidates(workerRecorders);
        workerRecorders = sorterW.getSortedCandidates();

        int systemSum = systemRecorders.stream().mapToInt(SystemRecorder::getUnallocatedCount).sum();
        int monitoredCountSum = workerRecorders.stream().mapToInt(WorkerRecorder::getMonitorCount).sum();
        int responsibility = (int) Math.ceil((systemSum + monitoredCountSum + workerRecorders.size() - 1) / workerRecorders.size());

        int workerIndex = 0;
        WorkerRecorder workerRecorder = workerRecorders.get(workerIndex);
        ArrayList<WorkAssignment> assignments = workerMappings.getMappingByWorker(workerRecorder);
        Iterator iterator = systemRecorders.iterator();

        int monitorCount = 0;
        SystemRecorder remainder = null;
        SystemRecorder systemRecorder;
        SystemInfo systemInfo = null;
        ArrayList<SystemSegment> segments = null;
        while (iterator.hasNext()) {
            if (remainder == null) {//init new systemRecorder
                systemRecorder = (SystemRecorder) iterator.next();
                systemInfo = getSystemInfoById(systemRecorder.getId());
                if (systemInfo.getHosts() == null) {
                    logger.info("allocateSystemToWorkers:find hostInfos=null;systemId={}", systemRecorder.getId());
                    break;
                }
                segments = systemMappings.getSystemMappingBySystemId(systemRecorder.getId());

            } else systemRecorder = remainder;//inherit systemRecorder from last run

            monitorCount = workerRecorder.getMonitorCount();
            if (monitorCount >= responsibility) {
                logger.info("allocateSystemToWorkers:finishes to allocate systems to worker ID={} with monitorCount={}",
                        workerRecorder.getId(), workerRecorder.getMonitorCount());
                if (monitorCountThredshold < monitorCount)
                    logger.warn("allocateSystemToWorkers: worker[{}] is overloaded; monitorCountThredshold={}, monitorCount={}",
                            workerRecorder.getId(), monitorCountThredshold, monitorCount);
                workerIndex += 1;
                if (workerIndex >= workerRecorders.size()) {
                    logger.info("allocateSystemToWorkers: all the workers (count={}) have been assigned. Stop allocation.", workerIndex);
                    break;
                }
                workerRecorder = workerRecorders.get(workerIndex);
                monitorCount = workerRecorder.getMonitorCount();
                assignments = workerMappings.getMappingByWorker(workerRecorder);
            }

            ArrayList<SystemSegment> unallocatedSegments = (ArrayList<SystemSegment>) segments.stream().filter(o -> o.getWorkerId() == null).collect(Collectors.toList());
            ArrayList<SystemSegment> segmentsToUpdate = (ArrayList<SystemSegment>) segments.stream().filter(o -> o.getWorkerId() != null).collect(Collectors.toList());
            int quota = responsibility - monitorCount;
            if (quota >= systemRecorder.getUnallocatedCount()) {
                for (SystemSegment s : unallocatedSegments) {
                    WorkAssignment assignment = new WorkAssignment();
                    assignment.setSystemId(systemRecorder.getId());
                    assignment.setHeader(s.getHeader());
                    assignment.setTrailer(s.getTrailer());
                    assignment.setCount(s.getCount());
                    assignments.add(assignment);
                    s.setWorkerId(workerRecorder.getId());
                }
                workerRecorder.setMonitorCount(monitorCount + systemRecorder.getUnallocatedCount());
                systemRecorder.setUnallocatedCount(0);
                systemRecorder.setAllocatedCount(systemInfo.getHosts().size());
                systemMappings.updateSystemSegments(systemRecorder, segments);

                remainder = null;
            } else {
                for (SystemSegment s : unallocatedSegments) {
                    WorkAssignment assignment = new WorkAssignment();
                    assignment.setSystemId(systemRecorder.getId());
                    assignment.setHeader(s.getHeader());
                    int size = systemInfo.getHostCount(s.getHeader(), s.getTrailer());
                    if (size <= quota) {
                        assignment.setTrailer(s.getTrailer());
                        assignment.setCount(size);
                        systemRecorder.setAllocatedCount(systemRecorder.getAllocatedCount() + size);
                        systemRecorder.setUnallocatedCount(systemRecorder.getUnallocatedCount() - size);
                        monitorCount += size;
                    } else {
                        HostInfo header = systemInfo.getHostInfoById(s.getHeader());
                        long id = systemInfo.getHostInfo(header, quota - 1).getId();
                        long headerId = systemInfo.getHostInfo(header, quota).getId();
                        assignment.setTrailer(id);
                        assignment.setCount(quota);
                        SystemSegment segmentSub = new SystemSegment();
                        segmentSub.setHeader(headerId);
                        segmentSub.setTrailer(s.getTrailer());
                        segmentSub.setCount(systemInfo.getHostCount(segmentSub.getHeader(), segmentSub.getTrailer()));
                        s.setTrailer(id);
                        s.setCount(quota);
                        segmentSub.setWorkerId(null);
                        segmentsToUpdate.add(segmentSub);
                        systemRecorder.setUnallocatedCount(systemRecorder.getUnallocatedCount() - quota);
                        systemRecorder.setAllocatedCount(systemRecorder.getAllocatedCount() + quota);
                        monitorCount += quota;

                    }
                    workerRecorder.setMonitorCount(monitorCount);
                    quota = responsibility - monitorCount;
                    assignments.add(assignment);
                    s.setWorkerId(workerRecorder.getId());
                    segmentsToUpdate.add(s);
                    if (quota <= 0)
                        break;
                }
                segments = segmentsToUpdate;
                systemMappings.updateSystemSegments(systemRecorder, segmentsToUpdate);
                remainder = systemRecorder;
            }
        }
        logger.info("allocateSystemToWorkers:finishes to allocate systems to worker ID={} with monitorCount={}",
                workerRecorder.getId(), workerRecorder.getMonitorCount());
        if (monitorCountThredshold < workerRecorder.getMonitorCount())
            logger.warn("allocateSystemToWorkers: worker[{}] is overloaded; monitorCountThredshold={}, monitorCount={}",
                    workerRecorder.getId(), monitorCountThredshold, workerRecorder.getMonitorCount());
        logger.info("allocateSystemToWorkers:finishes to allocate all the systems to workers");
        logger.info("renovateMappings_after allocateSystemToWorkers,workerMappings={}", workerMappings.toTabbedString());
        logger.info("renovateMappings_after allocateSystemToWorkers,systemMappings={}", systemMappings.toTabbedString());
    }

    private SystemSegment getSegmentByFeature(ArrayList<SystemSegment> segments, String workerId, long head) {
        for (SystemSegment s : segments) {
            if (s.getWorkerId().equals(workerId) && s.getHeader() == head) {
                return s;
            }
        }
        return null;
    }

    void rebalanceWorkerLoad() {
        ArrayList<WorkAssignment> workAssignmentpool = new ArrayList<>();
        Sorter<WorkerRecorder> sorter = new Sorter<>();
        sorter.setCandidates(workerRecorders);
        workerRecorders = sorter.getSortedCandidates();
        int monitorCountSum = 0;
        String monitorCountAsString = "";
        for (WorkerRecorder recorder : workerRecorders) {
            monitorCountSum += recorder.getMonitorCount();
            monitorCountAsString += recorder.getId() + ":" + recorder.getMonitorCount() + ",";
        }
        logger.info("rebalanceWorkerLoad: current load status: monitorCountSum={},load distributed={}", monitorCountSum, monitorCountAsString);
        int balancedLoad = (monitorCountSum + workerRecorders.size() - 1) / workerRecorders.size();
        float giniThreshold = pegaConfiguration.getGiniCoefficient();
        int maxLoad = (int) (balancedLoad * giniThreshold);
        logger.info("rebalanceWorkerLoad: ideal load : averageLoad={},maxLoad={}", balancedLoad, maxLoad);
        int currentMaxLoad = workerRecorders.get(getWorkerSize() - 1).getMonitorCount();
        if (currentMaxLoad <= maxLoad) {
            logger.info("rebalanceWorkerLoad: currentMaxload={} is no greater than required maxLoad. re-balance is unnecessary", currentMaxLoad, maxLoad);
            return;
        } else
            logger.info("rebalanceWorkerLoad: currentMaxload={} is  greater than required maxLoad. re-balance is required", currentMaxLoad, maxLoad);

        logger.info("rebalanceWorkerLoad:  re-balance begins ....");
        boolean sortPool = true;

        for (int i = workerRecorders.size() - 1; i >= 0; i--) {
            WorkerRecorder workerRecorder = workerRecorders.get(i);
            if (workerRecorder.getMonitorCount() <= maxLoad) {
                if (sortPool) {
                    sortPool = false;
                    Collections.sort(workAssignmentpool, new Comparator<WorkAssignment>() {
                        @Override
                        public int compare(WorkAssignment o1, WorkAssignment o2) {
                            return o1.getCount() > o2.getCount() ? -1 : (o1.getCount() == o2.getCount() ? 0 : 1);
                        }
                    });
                    logger.info("rebalanceWorkerLoad: collect eased work load count={},detail={}", workAssignmentpool.size(), workAssignmentpool.toString());
                }
                logger.info("rebalanceWorkerLoad:  re-balance worker={} workload={} by adding work load", workerRecorder.getId(), workerRecorder.getMonitorCount());
                int currentLoad = workerRecorder.getMonitorCount();
                int index = 0;
                ArrayList<WorkAssignment> balancedAssigments = new ArrayList<>();
                while (currentLoad <= balancedLoad) {
                    if (workAssignmentpool.size() <= index) {
                        logger.info("rebalanceWorkerLoad:  all the eased work load have been reassigned");
                        break;
                    }
                    balancedAssigments.add(workAssignmentpool.get(index));
                    logger.info("rebalanceWorkerLoad:  re-balance worker={} by adding work load ={}", workerRecorder.getId(), workAssignmentpool.get(index));
                    currentLoad += workAssignmentpool.get(index).getCount();
                    index++;
                }
                for (i = 0; i < index; i++) {
                    workAssignmentpool.remove(i);
                }
                while (currentLoad >= maxLoad) {
                    WorkAssignment assignment = balancedAssigments.get(balancedAssigments.size() - 1);
                    balancedAssigments.remove(balancedAssigments.size() - 1);
                    currentLoad -= assignment.getCount();
                    workAssignmentpool.add(0, assignment);
                }
                workerRecorder.setMonitorCount(currentLoad);
                balancedAssigments.addAll(workerMappings.getMappingByWorkerId(workerRecorder.getId()));
                logger.info("rebalanceWorkerLoad:  finish to re-balance worker={}  work load={} by adding work load", workerRecorder.getId(), workerRecorder.getMonitorCount());
                workerMappings.addWorkerMapping(workerRecorder, balancedAssigments);

            } else {
                logger.info("rebalanceWorkerLoad:  re-balance worker={}  work load={} by easing work load", workerRecorder.getId(), workerRecorder.getMonitorCount());
                ArrayList<WorkAssignment> workAssignments = workerMappings.getMappingByWorkerId(workerRecorder.getId());
                Collections.sort(workAssignments, new Comparator<WorkAssignment>() {
                    @Override
                    public int compare(WorkAssignment o1, WorkAssignment o2) {
                        return o1.getCount() > o2.getCount() ? -1 : (o1.getCount() == o2.getCount() ? 0 : 1);
                    }
                });
                int reservedLoad = 0;
                ArrayList<WorkAssignment> balancedAssigments = new ArrayList<>();
                for (WorkAssignment assignment : workAssignments) {
                    if (reservedLoad > maxLoad) {
                        int easedCount = workerRecorder.getMonitorCount() - assignment.getCount();
                        workerRecorder.setMonitorCount(easedCount);
                        logger.info("rebalanceWorkerLoad:  re-balance worker={} by easing work load={}", workerRecorder.getId(), assignment.toString());
                        workAssignmentpool.add(assignment);
                        continue;
                    }
                    reservedLoad += assignment.getCount();
                    balancedAssigments.add(assignment);
                }
                while (reservedLoad >= maxLoad) {
                    WorkAssignment assignment = balancedAssigments.get(balancedAssigments.size() - 1);
                    balancedAssigments.remove(balancedAssigments.size() - 1);
                    reservedLoad += assignment.getCount();
                    workAssignmentpool.add(0, assignment);
                }

                logger.info("rebalanceWorkerLoad:  finish to re-balance worker={}  work load={} reservedLoad={}  by easing work load", workerRecorder.getId(), workerRecorder.getMonitorCount(), reservedLoad);
                workerMappings.addWorkerMapping(workerRecorder, balancedAssigments);
            }
        }
        if (workAssignmentpool.size() != 0) {
            ArrayList<WorkAssignment> workAssignments = workerMappings.getMappingByWorkerId(workerRecorders.get(0).getId());
            int leftCount = 0;
            for (int i = 0; i < workAssignmentpool.size(); i++)
                leftCount += workAssignmentpool.get(i).getCount();
            workAssignmentpool.addAll(workAssignments);
            workerRecorders.get(0).setMonitorCount(workerRecorders.get(0).getMonitorCount() + leftCount);
            logger.info("rebalanceWorkerLoad:  finish to re-balance left work load to worker={}  work load={} ", workerRecorders.get(0).getId(), workerRecorders.get(0).getMonitorCount());
            workerMappings.addWorkerMapping(workerRecorders.get(0), workAssignmentpool);
        }
        logger.info("rebalanceWorkerLoad:  re-balance ends");

    }

    void regenerateSystemMappings() {
        logger.info("regenerateSystemMappings begins");
        SystemMappings systemMappings = new SystemMappings();
        this.initSystemRecorders(this.systemInfos, systemMappings);
        logger.info("regenerateSystemMappings: finishes to init sytemRecords for all the systems");
        ArrayList<SystemRecorder> systemRecorders = systemMappings.getSystemRecorders();
        for (SystemRecorder systemRecorder : systemRecorders) {
            logger.info("regenerateSystemMappings: begins to generate system mapping for system id={}", systemRecorder.getId());
            ArrayList<SystemSegment> segments = new ArrayList<>();
            for (WorkerRecorder workerRecorder : this.workerRecorders) {
                ArrayList<WorkAssignment> workAssignments = workerMappings.getMappingByWorkerId(workerRecorder.getId());
                ArrayList<WorkAssignment> filteredAssignments = (ArrayList<WorkAssignment>) workAssignments.stream().filter(o -> o.getSystemId() == systemRecorder.getId()).collect(Collectors.toList());
                if (filteredAssignments == null || filteredAssignments.size() == 0)
                    continue;
                logger.info("regenerateSystemMappings: start  to add assignments from worker={} to system id={}", workerRecorder.getId(), systemRecorder.getId());
                for (WorkAssignment assignment : filteredAssignments) {
                    SystemSegment systemSegment = new SystemSegment();
                    systemSegment.setWorkerId(workerRecorder.getId());
                    systemSegment.setTrailer(assignment.getTrailer());
                    systemSegment.setHeader(assignment.getHeader());
                    systemSegment.setCount(assignment.getCount());
                    segments.add(systemSegment);
                    systemRecorder.setAllocatedCount(systemRecorder.getAllocatedCount() + assignment.getCount());
                    systemRecorder.setUnallocatedCount(systemRecorder.getUnallocatedCount() - assignment.getCount());
                    logger.info("regenerateSystemMappings: finishes to add assignment (count={},head={},tail={}) from worker id={} to system id={}", assignment.getCount(), assignment.getHeader(),
                            assignment.getTrailer(), workerRecorder.getId(), systemRecorder.getId());
                }
                logger.info("regenerateSystemMappings: finishes to add assignments  from worker={} to  system id={}", workerRecorder.getId(), systemRecorder.getId());
            }
            systemMappings.addSystemMapping(systemRecorder, segments);
            logger.info("regenerateSystemMappings: finishes to add assignments  system id={}", systemRecorder.getId());
        }
        logger.info("regenerateSystemMappings: update systemMappings for mapping manager");
        this.systemMappings = systemMappings;
        logger.info("regenerateSystemMappings: finishes to generate systemSegments for all the system. Process ends");
    }

    void loadBalanceAmongWorkers() {
        Sorter<WorkerRecorder> sorter = new Sorter<>();
        sorter.setCandidates(workerRecorders);
        workerRecorders = sorter.getSortedCandidates();
        WorkerRecorder least = workerRecorders.get(0);
        WorkerRecorder most = workerRecorders.get(getWorkerSize() - 1);
        float gini = (float) most.getMonitorCount() / (1 + least.getMonitorCount());
        float giniThreshold = pegaConfiguration.getGiniCoefficient();
        while (gini > giniThreshold) {
            logger.info("loadBalanceAmongWorkers: begins new turn:gini={}", gini);
            int average = (most.getMonitorCount() + least.getMonitorCount()) / 2;
            ArrayList<WorkAssignment> most_assignments = workerMappings.getMappingByWorkerId(most.getId());
            Sorter<WorkAssignment> sorterW = new Sorter<>();
            sorterW.setCandidates(most_assignments);
            most_assignments = sorterW.getSortedCandidates();
            ArrayList<WorkAssignment> least_assigments = workerMappings.getMappingByWorkerId(least.getId());
            int index = 0;
            while (most.getMonitorCount() > average) {
                WorkAssignment assignment = most_assignments.get(index);
                ArrayList<SystemSegment> segments = systemMappings.getSystemMappingBySystemId(assignment.getSystemId());
                SystemSegment segment = getSegmentByFeature(segments, most.getId(), assignment.getHeader());
                logger.info("loadBalanceAmongWorkers: begins new turn, workerRecorders information={},assignment information={}", workerRecorders.toString(), assignment.toString());
                int sign = most.getMonitorCount() - assignment.getCount() - average;
                if (sign >= 0) {
                    most.setMonitorCount(most.getMonitorCount() - assignment.getCount());
                    least.setMonitorCount(least.getMonitorCount() + assignment.getCount());
                    assert segment != null;
                    segment.setWorkerId(least.getId());
                    least_assigments.add(assignment);
                    most_assignments.remove(assignment);
                    systemMappings.updateSystemSegments(assignment.getSystemId(), segments);
                } else {
                    int splitCount = assignment.getCount() + average - most.getMonitorCount();
                    WorkAssignment assignment1 = new WorkAssignment();
                    assignment1.setSystemId(assignment.getSystemId());
                    assignment1.setCount(assignment.getCount() - splitCount);
                    assignment1.setTrailer(assignment.getTrailer());
                    assignment.setCount(splitCount);
                    SystemInfo info = getSystemInfoById(assignment.getSystemId());
                    try {
                        long newTail = info.getHostInfo(assignment.getHeader(), splitCount - 1).getId();
                        long newHead = info.getHostInfo(assignment.getHeader(), splitCount).getId();
                        assignment.setTrailer(newTail);
                        assignment1.setHeader(newHead);
                        least_assigments.add(assignment1);

                        SystemSegment segment1 = new SystemSegment();
                        segment1.setWorkerId(least.getId());
                        segment1.setCount(assignment1.getCount());
                        segment1.setHeader(assignment1.getHeader());
                        segment1.setTrailer(assignment1.getTrailer());
                        segment.setTrailer(newTail);
                        segment.setCount(splitCount);
                        segments.add(segment1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.info("loadBalanceAmongWorkers throws exception: splitCount={},most count={},least count={}", splitCount, most.getMonitorCount(), least.getMonitorCount());
                    }
                    systemMappings.updateSystemSegments(assignment.getSystemId(), segments);
                    most.setMonitorCount(most.getMonitorCount() - assignment1.getCount());
                    least.setMonitorCount(least.getMonitorCount() + assignment1.getCount());
                }
            }
            workerMappings.updateMapping(most, most_assignments);
            workerMappings.updateMapping(least, least_assigments);
            sorter.setCandidates(workerRecorders);
            workerRecorders = sorter.getSortedCandidates();
            least = workerRecorders.get(0);
            most = workerRecorders.get(getWorkerSize() - 1);
            gini = (float) most.getMonitorCount() / least.getMonitorCount();
        }


    }

    private SystemInfo getSystemInfoById(long id) {
        for (SystemInfo systemInfo : systemInfos) {
            if (systemInfo.getId() == id) {
                return systemInfo;
            }
        }
        return null;
    }

    private void loadSystemMappingsFromZKTree() {
        ObjectMapper mapper = new ObjectMapper();
        String systemPath = pegaConfiguration.getSystemPath();
//        List<String> children = ZookeeperUtil.getInstance().getChildren(systemPath);
        for (SystemInfo systemInfo : systemInfos) {
            String wholePath = ZookeeperUtil.getInstance().concatPath(systemPath, String.valueOf(systemInfo.getId()));
            boolean ifExists = ZookeeperUtil.getInstance().checkExists(wholePath);

            if (ifExists) {
                SystemRecorder systemRecorder = new SystemRecorder();
                systemRecorder.setId(systemInfo.getId());
                systemRecorder.setUptime(null);
                systemRecorder.setState(PegaEnum.ObjectState.valid);
                String segmentString = ZookeeperUtil.getInstance().getData(wholePath);
                logger.info("loadSystemMappingsFromZKTree:segmentString={},system id={}", segmentString, systemRecorder.getId());
                if (segmentString == null) {
                    logger.info("loadSystemMappingsFromZKTree: sytemId={} has no segements", systemInfo.getId());
                    continue;
                }
                JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, SystemSegment.class);
                try {
                    ArrayList<SystemSegment> assignments = mapper.readValue(segmentString, type);
                    int count = assignments.stream().filter(o -> o.getWorkerId() != null).mapToInt(o -> o.getCount()).sum();
                    systemRecorder.setAllocatedCount(count);
                    count = assignments.stream().filter(o -> o.getWorkerId() == null).mapToInt(o -> o.getCount()).sum();
                    systemRecorder.setUnallocatedCount(count);
                    systemMappings.addSystemMapping(systemRecorder, assignments);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadWorkerMappingsFromZKTree() {
        ObjectMapper mapper = new ObjectMapper();
        String mappingPath = pegaConfiguration.getMappingPath();
        List<String> workerIds = ZookeeperUtil.getInstance().getChildren(mappingPath);
        for (String workerId : workerIds) {
            String mappings = ZookeeperUtil.getInstance().getData(ZookeeperUtil.getInstance().concatPath(mappingPath, workerId));
            if (mappings == null) {
                logger.info("loadWorkerMappingsFromZKTree: workerId={} has no segements", workerId);
                continue;
            }
            WorkerRecorder recorder = getWorkerRecorderById(workerId);
            if (recorder == null) {
                recorder = new WorkerRecorder();
                recorder.setId(workerId);
                recorder.setState(PegaEnum.ObjectState.valid);
            }
            JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, WorkAssignment.class);
            try {
                ArrayList<WorkAssignment> assignments = mapper.readValue(mappings, type);
                int count = assignments.stream().mapToInt(o -> o.getCount()).sum();
                recorder.setMonitorCount(count);
                workerMappings.addWorkerMapping(recorder, assignments);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void addWorker(WorkerRecorder workerRecorder) {
        workerRecorders.add(workerRecorder);
        ArrayList<WorkAssignment> assignments = new ArrayList<>();
        workerMappings.addWorkerMapping(workerRecorder, assignments);
    }

    void updateWorkerState(String id, PegaEnum.ObjectState state, Date uptime) {
        for (WorkerRecorder worker : workerRecorders) {
            if (worker.getId().equals(id)) {
                worker.setRecordTime(uptime);
                worker.setState(state);
            }
        }
    }

    void setSystemInfos(ArrayList<SystemInfo> systemInfos) {
        this.systemInfos = systemInfos;
    }

    WorkerMappings getWorkerMappings() {
        return workerMappings;
    }

}
