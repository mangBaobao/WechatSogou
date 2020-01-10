package cert.aiops.pega.masterExecutors;

import cert.aiops.pega.channels.ChannelManager;
import cert.aiops.pega.registratedHostManagement.RegisteredHostManager;
import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.bean.SystemInfo;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.innerService.HostInfoService;
import cert.aiops.pega.innerService.SystemInfoService;
import cert.aiops.pega.startup.BeingMasterCondition;
import cert.aiops.pega.synchronization.Synchronizer;
import cert.aiops.pega.util.SpringContextUtil;
import cert.aiops.pega.util.ZookeeperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Conditional(value = {BeingMasterCondition.class})
public class Master {
    private Logger logger = LoggerFactory.getLogger(Master.class);


    @Autowired
    PegaConfiguration pegaConfiguration;
    @Autowired
    private Synchronizer synchronizer;
    private PegaEnum.NodeRole role;
    private long epoch;
    private String id;
    private MasterPathChildrenCacheListener workersListener;
    private ArrayList<SystemInfo> systemInfos;
    @Autowired
    private MappingManager mappingManager;

    @Autowired
    private MessageQueueManager messageQueueManager;

    @Autowired
    private RegisteredHostManager registeredHostManager;

    @Autowired
    private ChannelManager channelManager;

    MappingManager getMappingManager() {
        return mappingManager;
    }


    public Master() {
        workersListener = new MasterPathChildrenCacheListener();
        systemInfos = new ArrayList<>();
    }

    public void init(PegaEnum.State state, String[] systemNames) {
        id = pegaConfiguration.getId();
        establishIdentity();
        if (role == PegaEnum.NodeRole.online) {
            initNodesInZKTree();
            initializeEpoch();
            initializeSystemInfos(state, systemNames);
            constructSystemsTree();
            mappingManager.setSystemInfos(systemInfos);
            mappingManager.initAllMappings();
            messageQueueManager.updateQueueStatus();
            channelManager.init();
            registeredHostManager.firstPublishIdentification();
        }
    }

    private void initNodesInZKTree() {
        boolean ifExists = ZookeeperUtil.getInstance().checkExists(pegaConfiguration.getWorkerPath());
        if (!ifExists) {
            String result = ZookeeperUtil.getInstance().createPersistenceNodeWithoutValue(pegaConfiguration.getWorkerPath());
            if (result == null) {
                logger.error("Master initNodesInZKTree fail:path={};reason=null", pegaConfiguration.getWorkerPath());
                System.exit(0);
            }
        }
        ifExists = ZookeeperUtil.getInstance().checkExists(pegaConfiguration.getMappingPath());
        if (!ifExists) {
            String result = ZookeeperUtil.getInstance().createPersistenceNodeWithoutValue(pegaConfiguration.getMappingPath());
            if (result == null) {
                logger.error("Master initNodesInZKTree fail: createPersistenceNodeWithoutValue fail: path:{}", pegaConfiguration.getMappingPath());
                System.exit(0);
            }
        }
        ifExists = ZookeeperUtil.getInstance().checkExists(pegaConfiguration.getSystemPath());
        if (!ifExists) {
            String base = ZookeeperUtil.getInstance().createPersistenceNodeWithoutValue(pegaConfiguration.getSystemPath());
            if (base == null) {
                logger.error("Master_constructHostZKTree fail: createPersistenceNodeWithoutValue fail: path:{}", pegaConfiguration.getSystemPath());
                System.exit(0);
            }
        }
    }

    public String getId() {
        return id;
    }

    private boolean becomeOnline() {
    //    String nodePath=ZookeeperUtil.getInstance().concatPath(pegaConfiguration.getControllerPath(),id);
        String result = ZookeeperUtil.getInstance().createEphemeralNode(pegaConfiguration.getControllerPath(), id);
        if (result != null) {
            role = PegaEnum.NodeRole.online;
            pegaConfiguration.setRole(String.valueOf(role));
            logger.info("Master_establishIdentity: id ({}) becomes online,path={}", id,pegaConfiguration.getControllerPath());
            return true;
        }
        return false;
    }

    private void establishIdentity() {
        boolean ifMasterExists = ZookeeperUtil.getInstance().checkExists(pegaConfiguration.getControllerPath());
        if (!ifMasterExists) {
            boolean success = becomeOnline();
            if (!success) {
                logger.info("Master_establishIdentity: success={}. System is going to exit", success);
                System.exit(0);
            }
        } else {
            String masterId = ZookeeperUtil.getInstance().getData(pegaConfiguration.getControllerPath());
            if (masterId.equals(id)) {
                role = PegaEnum.NodeRole.online;
                pegaConfiguration.setRole(String.valueOf(role));
                logger.info("Master_establishIdentity: id ({}) becomes online", id);
            } else {
                logger.info("Master_establishIdentity: id={} exists but not equal to self id ={}. System is going to exit", id, masterId);
                System.exit(0);
            }
        }
    }

    public SystemInfo getSystemInfoByName(String systemName) {
        for (SystemInfo info : systemInfos) {
            if (info.getSystemName().equals(systemName))
                return info;
        }
        return null;
    }

    public long getEpoch() {
        return epoch;
    }

    public ArrayList<SystemInfo> getSystemInfos() {
        return systemInfos;
    }

    private void initializeEpoch() {
        boolean ifEpochExist = ZookeeperUtil.getInstance().checkExists(pegaConfiguration.getRoutineEpochPath());
        if (!ifEpochExist) {
            epoch = 0;
            ZookeeperUtil.getInstance().createPersistenceNode(pegaConfiguration.getRoutineEpochPath(), String.valueOf(epoch));
        } else
            epoch = Long.valueOf(ZookeeperUtil.getInstance().getData(pegaConfiguration.getRoutineEpochPath()));
        logger.info("Master_initializeEpoch: id={} successfully create epoch node in zkTree", pegaConfiguration.getRoutineEpochPath());
    }

    void updateEpoch() {
        epoch++;
        ZookeeperUtil.getInstance().setData(pegaConfiguration.getRoutineEpochPath(), String.valueOf(epoch));
        logger.info("updateEpoch: finishes updating epoch to {}", epoch);
    }

    private void initializeSystemInfos(PegaEnum.State state, String[] systemNames) {
        SystemInfoService systemInfoService = SpringContextUtil.getBean(SystemInfoService.class);
        ArrayList<SystemInfo> synchronizedInfos = new ArrayList<>();
        systemInfos = systemInfoService.getAllSystemInfos();
        //     if (systemInfos.size() == 0) {
        if (systemNames == null || systemNames.length == 0)
            synchronizedInfos.addAll(synchronizer.syncSystemsByState(state.toString()));
        else {
            for (String systemName : systemNames) {
                SystemInfo info = synchronizer.syncSystemByName(state.toString(), systemName);
                if (info == null)
                    continue;
                if (info.getHosts() != null)
                    synchronizedInfos.add(info);
            }
        }

        ArrayList<SystemInfo> duplicatedSystemInfos=new ArrayList<>();
        for(int i =0;i<synchronizedInfos.size();i++){
           long id=synchronizedInfos.get(i).getId();
           for(int j=0;j<systemInfos.size();j++){
               long id_db=systemInfos.get(j).getId();
               if(id==id_db){
                   logger.info("Master_initializeSystemInfos: find duplicated system id={}, name={}, delete from synchronized infos",id, synchronizedInfos.get(i).getSystemName());
                   duplicatedSystemInfos.add(synchronizedInfos.get(i));
               }
           }
        }
        synchronizedInfos.removeAll(duplicatedSystemInfos);

        //store systeminfo and hostinfo to db
        HostInfoService hostInfoService = SpringContextUtil.getBean(HostInfoService.class);
        for (SystemInfo info : synchronizedInfos) {
            systemInfoService.addSystem(info);
            hostInfoService.addHostInfoList(info.getHosts());
        }
        //}
        systemInfos.addAll(synchronizedInfos);
    }
/**
 * todo increamental synchronization with jczy
 */
//    private ArrayList<SystemInfo>  mergeSystemInfosWithSynchronization(ArrayList<SystemInfo>  synchronizedInfos){
//        if (systemInfos == null)
//            return synchronizedInfos;
//
//        if
//    }

    /**
     * input arraylist<systeminfo>  pojos, and maps them into zk tree as /systems/sys1/hostIp,
     * where sys1 records total number of hosts, and host records host name and host state  etc.
     */
    private void constructSystemsTree() {
//        boolean ifExists=ZookeeperUtil.getInstance().checkExists(pegaConfiguration.getSystemPath());
//        if(ifExists==false)
//        ObjectMapper mapper = new ObjectMapper();
        ZookeeperUtil.getInstance().createPersistenceNodeWithoutValue(pegaConfiguration.getSystemPath());
        ZookeeperUtil.getInstance().setData(pegaConfiguration.getSystemPath(), String.valueOf(systemInfos.size()));
        List<String> children = ZookeeperUtil.getInstance().getChildren(pegaConfiguration.getSystemPath());
        if (children == null) {
            children = new ArrayList<>();
        }
        for (SystemInfo systemInfo : systemInfos) {
            if (children.contains(String.valueOf(systemInfo.getId())))
                continue;
            //    if (systemInfo.getHostCount() != 0) {
            String parentPath = ZookeeperUtil.getInstance().concatPath(pegaConfiguration.getSystemPath(), String.valueOf(systemInfo.getId()));
            String result = ZookeeperUtil.getInstance().createPersistenceNode(parentPath, "");
            if (result == null) {
                logger.error("Master_constructSystemTree fail: createPersistenceNodeWithoutValue fail: path:{}", parentPath);
                System.exit(0);
            }
            //   }
        }
    }
}
