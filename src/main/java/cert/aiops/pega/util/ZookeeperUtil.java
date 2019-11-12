package cert.aiops.pega.util;

import cert.aiops.pega.config.ZKConfiguration;
import cert.aiops.pega.masterExecutors.PegaNodeCacheListener;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * wrap zookeeper client, currently use Netflix Curator
 */
@Component
public class ZookeeperUtil {
    Logger logger = LoggerFactory.getLogger(ZookeeperUtil.class);

    @Autowired
    private ZKConfiguration zkConfiguration;

    private static ZookeeperUtil zookeeperUtil;
    private  RetryPolicy retryPolicy;
    private  CuratorFramework curator;

    @PostConstruct
   public void init(){
        zookeeperUtil =this;
        zookeeperUtil.zkConfiguration=this.zkConfiguration;
        System.out.println(zkConfiguration);
        retryPolicy =new ExponentialBackoffRetry(zkConfiguration.getBaseSleepTimeMs(),zkConfiguration.getMaxRetries(),zkConfiguration.getMaxSleepMs());
//        curator= CuratorFrameworkFactory.newClient(zkConfiguration.getConnectString(),zkConfiguration.getSessionTimeoutMs(),zkConfiguration.getConnectionTimeoutMs(),retryPolicy);
        curator = CuratorFrameworkFactory.builder()
                .connectString(zkConfiguration.getConnectString())
                .sessionTimeoutMs(zkConfiguration.getSessionTimeoutMs())
                .connectionTimeoutMs(zkConfiguration.getConnectionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(zkConfiguration.getChroot())
                .build();
        curator.start();
        curator.getChildren();
   }

   public void disconnect(){
        curator.close();
   }

   public  static ZookeeperUtil getInstance(){
        if(zookeeperUtil ==null){
            System.out.println("zkoperationutil is null");
            return null;
        }
        return zookeeperUtil;
   }

   public String  createPersistenceNode(String path, String nodeValue){
        try{
            return curator.create().creatingParentsIfNeeded()
                    .forPath(path,nodeValue.getBytes());
        }catch (Exception e){
            logger.error("createPersistenceNode fail: nodePath:{},nodeValue:{};reason:{}",path,nodeValue,e.getMessage());
         //   e.printStackTrace();
        }
        return null;
   }

    public String  createPersistenceNodeWithoutValue(String path){
        try{
            if(checkExists(path))
                return path;
            return curator.create().creatingParentsIfNeeded()
                    .forPath(path);
        }catch (Exception e){
            logger.error("createPersistenceNodeWithoutValue fail: nodePath:{};reason:{}",path,e.getMessage());
          //  e.printStackTrace();
        }
        return null;
    }

   public String  createEphemeralNode(String path,String nodeValue){
        try{
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(path,nodeValue.getBytes());
        }catch (Exception e){
            logger.error("createEphemeralNode fail:nodePath:{},nodeValue:{};reason:{}",path,nodeValue,e.getMessage());
          e.printStackTrace();
        }
        return null;
   }

   public String createSequentialPersistenceNode(String path, String nodeValue){
        try{
            return curator.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(path,nodeValue.getBytes());
        }catch (Exception e){
            logger.error("createSequentialPersistenceNode fail: nodePath:{},nodeValue:{};reason:{}",path,nodeValue,e.getMessage());
        }
        return null;
   }

   public String createSequentialEphermalNode(String path,String nodeValue){
        try{
            return curator.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(path,nodeValue.getBytes());
        }catch (Exception e){
            logger.error("createSequentialEphermalNode fail:nodePath:{},nodeValue:{};reason:{}",path,nodeValue,e.getMessage());
           // e.printStackTrace();
        }
        return null;
   }
   public void setData(String path, String nodeValue){
        try{
        //    this.deleteNode(path);
            curator.setData().forPath(path,nodeValue.getBytes());
        }catch (Exception e){
            logger.error("setData fail:nodePath:{},nodeValue:{};reason:{}",path,nodeValue,e.getMessage());
            //e.printStackTrace();
        }
    }

   public void  deleteNode(String path)   {
        try{
            curator.delete().guaranteed().forPath(path);
        }catch(Exception e){
            logger.error("deleteNode fail:nodePath:{};reason:{}",path,e.getMessage());
            //e.printStackTrace();
        }
   }

   public void deleteChildrenIfNeeded(String path){
        try{
            curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        }catch (Exception e){
            logger.error("deleteChildrenIfNeeded fail:nodePath:{};reason:{}",path, e.getMessage());
          //  e.printStackTrace();
        }
   }

   public String getData(String path){
        try{
            return new String(curator.getData().forPath(path));
        }catch(Exception e){
            logger.error("getData fail:nodePath:{};reason:{}",path,e.getMessage());
          //  e.printStackTrace();
        }
        return null;
   }

   public boolean checkExists(String path){
        try{
            Stat stat = curator.checkExists().forPath(path);
            return stat !=null;
        }catch (Exception e){
            logger.error("checkExists fail:nodePath:{};reason:{}",path,e.getMessage());
           // e.printStackTrace();
        }
        return false;
   }

public List<String>  getChildren(String path){
        try{
            return curator.getChildren().forPath(path);
        }catch(Exception e){
            logger.error("getChildren fail:nodepath:{};reason:{}",path,e.getMessage());
          //  e.printStackTrace();
        }
        return null;
}

public void  registerNodeCacheListener(String path, NodeCacheListener listener){
        try{
            NodeCache nodeCache = new NodeCache(curator,path);
            nodeCache.getListenable().addListener(listener);
           ((PegaNodeCacheListener)listener).setNodeCache(nodeCache);
            nodeCache.start(true);
        }catch (Exception e){
            logger.error("registerNodeCacheListener fail:nodePath:{};reason:{}",path,e.getMessage());
           // e.printStackTrace();
        }
}

public PathChildrenCache registerPathChildListener(String path, PathChildrenCacheListener listener){
        try{
            PathChildrenCache cache = new PathChildrenCache(curator,path,true);
            cache.getListenable().addListener(listener);
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            return cache;
        }catch (Exception e){
            logger.error("registerPathChildListener fail:nodePath:{};reason:{}",path,e.getMessage());
            //e.printStackTrace();
        }
        return null;
}

    public PathChildrenCache removePathChildListener(PathChildrenCache cache, PathChildrenCacheListener listener){
        try{
            cache.getListenable().removeListener(listener);
            cache.rebuild();
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            return cache;
        }catch (Exception e){
            logger.error("removePathChildListener fail;reason:{}",e.getMessage());
        }
        return null;
    }

public TreeCache registerTreeCacheListener(String path, int depth, TreeCacheListener listener){
        try{
            TreeCache cache = TreeCache.newBuilder(curator,path)
                    .setCacheData(true).setMaxDepth(depth).build();
            cache.getListenable().addListener(listener);
            cache.start();
            return cache;
        }catch (Exception e){
            logger.error("registerTreeCacheListener fail: nodePath:{},depth:{};reason:{}",path,depth,e.getMessage());
            e.printStackTrace();
        }
        return null;
}

public String concatPath(String parent, String child){
        return parent+"/"+child;
}

public Stat getNodeStat(String path){
        Stat stat=new Stat();
    try {
        curator.getData().storingStatIn(stat).forPath(path);
        return stat;
    } catch (Exception e) {
        logger.info("getNodeStat:fail;path=[],reason={}",path,e.getMessage());
       // e.printStackTrace();
    }
    return  null;
}
}
