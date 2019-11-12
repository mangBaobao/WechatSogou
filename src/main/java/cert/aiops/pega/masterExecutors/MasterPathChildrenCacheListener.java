package cert.aiops.pega.masterExecutors;

import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.bean.mapping.WorkerRecorder;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.util.SpringContextUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
@Deprecated
public class MasterPathChildrenCacheListener implements PathChildrenCacheListener {
    Logger logger = LoggerFactory.getLogger(MasterPathChildrenCacheListener.class);
    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        switch (event.getType()){
            case CHILD_ADDED: {
                WorkerRecorder workerRecorder = new WorkerRecorder();
                String nodeId=getNodeId(event.getData().getPath());
                if(nodeId==null){
                    logger.error("MasterPathChildrenCacheListener event:child added;nodeId=null inqualified;will be omitted");
                    break;
                }
//                PegaEnum.Net net = getNet(event.getData().getPath());
//                if(net==null){
//                    logger.error("MasterPathChildrenCacheListener event:child added;net=null inqualified; will be omitted");
//                }
                workerRecorder.setId(nodeId);
//                workerRecorder.setWorkingNet(net);
                workerRecorder.setState(PegaEnum.ObjectState.valid);
//                workerRecorder.setUptime(new Date());
                workerRecorder.setRecordTime(new Date());
                workerRecorder.setMonitorCount(0);
                Master master= SpringContextUtil.getBean(Master.class);
                master.getMappingManager().addWorker(workerRecorder);
                logger.info("MasterPathChildrenCacheListener event:child added;nodeId={}",nodeId);
                break;
            }
            case CHILD_REMOVED:{
                String nodeId=getNodeId(event.getData().getPath());
                if(nodeId==null){
                    logger.error("MasterPathChildrenCacheListener event:child removed;nodeId=null inqualified;will be omitted");
                    break;
                }
                Master master = SpringContextUtil.getBean(Master.class);
                master.getMappingManager().updateWorkerState(nodeId, PegaEnum.ObjectState.invalid,new Date());
                logger.info("MasterPathChildrenCacheListener event:child removed;nodeId={}",nodeId);
                break;
            }
        }
    }

    private String getNodeId(String fullPath){
        String nodeId=null;
        PegaConfiguration configuration = SpringContextUtil.getBean(PegaConfiguration.class);
        if(fullPath.contains(configuration.getWorkerPath()))
            nodeId=fullPath.replace(configuration.getWorkerPath()+"/","");
        return nodeId;
    }

//    private PegaEnum.Net getNet(String fullPath){
//        PegaConfiguration configuration = SpringContextUtil.getBean(PegaConfiguration.class);
//        if(fullPath.contains(configuration.getWorkerInVPath()))
//            return PegaEnum.Net.v;
//        if(fullPath.contains(configuration.getWorkerInZPath()))
//            return PegaEnum.Net.z;
//        return null;
//
//    }
}
