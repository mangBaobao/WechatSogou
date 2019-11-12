package cert.aiops.pega.masterExecutors;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Deprecated
public class PegaNodeCacheListener implements NodeCacheListener {
    Logger logger = LoggerFactory.getLogger(PegaNodeCacheListener.class);

    NodeCache nodeCache;
    ChildData childData;
    public void setNodeCache(NodeCache nodeCache){
        this.nodeCache=nodeCache;
    }

    public NodeCache getNodeCache(){
        return nodeCache;
    }

    @Override
    public void nodeChanged() throws Exception {
        childData = nodeCache.getCurrentData();
        if(childData == null)
            logger.error("PegaNodeCacheListener nodeChanged error: childData is null; node is non-exist");
        else{
            logger.info("PegaNodeCacheListener childData path:{},Stat:{},Data:{}",
                    childData.getPath(),childData.getStat(),new String(childData.getData()));
        }
    }

    public ChildData getChildData(){
        return childData;
    }
}
