package cert.aiops.pega;

import cert.aiops.pega.bean.*;
import cert.aiops.pega.bean.mapping.*;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.dao.HostInfoClickDao;
import cert.aiops.pega.dao.HostStateDao;
import cert.aiops.pega.dao.SystemMappingsDao;
import cert.aiops.pega.dao.WorkerMappingsDao;
import cert.aiops.pega.masterExecutors.Master;
import cert.aiops.pega.masterExecutors.MasterCronTasks;
import cert.aiops.pega.masterExecutors.PegaNodeCacheListener;
import cert.aiops.pega.service.JczySynchronizationService;
import cert.aiops.pega.synchronization.*;
import cert.aiops.pega.workerExecutors.Worker;
import cert.aiops.pega.service.HostInfoService;
import cert.aiops.pega.service.SystemInfoService;
import cert.aiops.pega.util.*;
import com.google.gson.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PegaApplicationTest {

    @Autowired
    PegaConfiguration pega;

    @Mock
    JczySynchronizationService jczySynchronizationService;

    Logger logger = LoggerFactory.getLogger(PegaApplicationTest.class);


    @Test
    public void springContextUtilTest() {
        if (SpringContextUtil.containsBean("master")) {
            Master master = SpringContextUtil.getBean("master");
        }

        if (SpringContextUtil.containsBean("worker")) {
            Worker worker = SpringContextUtil.getBean("worker");
        }
    }

    @Test
    public void redisUtilTest() {
        //create entities
//        ArrayList<HostState> hosts = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            HostState state = new HostState();
//            state.setIp(String.valueOf(i));
//            state.setNet(String.valueOf(i * 2));
//            state.setStatus(i);
//            state.setTimestamp(String.valueOf(new Date()));
//            hosts.add(state);
//        }
//
//        String strKey = "1_sys1_count";
//        int strValue = 1001;
//
//        String tempKey = String.valueOf(new Date().getTime());
//        String routineKey = "1_sys1";
//
//        logger.info("redisClientUtil test now begins...");
//        logger.info("redisClientUtil test: setStr");
//        RedisClientUtil.getInstance().setStr(strKey, String.valueOf(strValue));
//        logger.info("redisClientUtil test:getStr");
//        String value = RedisClientUtil.getInstance().getStr(strKey);
//        logger.info("redisClientUtil test: getStr:{}", value);
//        logger.info("redisClientUtil test:setObj");
//        RedisClientUtil.getInstance().setObj(tempKey, hosts);
//        logger.info("redisClientUtil test:getObj");
//        ArrayList<HostState> hostValues = (ArrayList<HostState>) RedisClientUtil.getInstance().getObj(tempKey);
//        logger.info("redisClientUtil test:getObj: {}", hostValues.get(0));
//        logger.info("redisClientUtil test:ifstrKeyExists:{}", RedisClientUtil.getInstance().ifStrKeyExists(strKey));
//        logger.info("redisClientUtil test:ifobjKeyExists:{}", RedisClientUtil.getInstance().ifObjKeyExists(tempKey));
//        logger.info("redisClientUtil test: expire str key to 5s:{}", RedisClientUtil.getInstance().expire(strKey, 5));
//        logger.info("redisClientUtil test: expire obj key to 5s:{}", RedisClientUtil.getInstance().expireObj((Object) tempKey, 5));
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        logger.info("redisClientUtil test:ifstrKeyExists:{}", RedisClientUtil.getInstance().ifStrKeyExists(strKey));
//        logger.info("redisClientUtil test:ifobjKeyExists:{}", RedisClientUtil.getInstance().ifObjKeyExists(tempKey));
//        logger.info("redisClientUtil test: addList");
//        for (int i = 0; i < 10; i++) {
//            RedisClientUtil.getInstance().addList(routineKey, hosts.get(i));
//        }
//        logger.info("redisClientUtil test:ifListKeyExists:{}", RedisClientUtil.getInstance().ifListKeyExists(routineKey));
//        logger.info("redisClientUtil test:getListSize:{}", RedisClientUtil.getInstance().getListSize(routineKey));
//        logger.info("redisClientUtil test: getListPage");
//        hostValues = (ArrayList<HostState>) RedisClientUtil.getInstance().getListPage(routineKey, 0, 4);
//        logger.info("redisClientUtil test:getListPage:{}", hostValues.get(4));
//        logger.info("redisClientUtil test: getListPage");
//        hostValues = (ArrayList<HostState>) RedisClientUtil.getInstance().getListPage(routineKey, 5, 19);
//        logger.info("redisClientUtil test:getListPage:{}", hostValues.get(1));
//        logger.info("redisClientUtil test:delList");
//        RedisClientUtil.getInstance().delList(routineKey);
//        logger.info("redisClientUtil test:ifstrKeyExists:{}", RedisClientUtil.getInstance().ifListKeyExists(routineKey));
//        logger.info("redisClientUtil test:getListSize:{}", RedisClientUtil.getInstance().getListSize(routineKey));

    }

    @Test
    public void messageUtilTest() {
        logger.info("messageUtil test now begins..................");
        logger.info("ifQueueExist:{}", MessageUtil.getInstance().ifQueueExist("w_192.168.1.6"));
        RequestTask task = new RequestTask();
        task.setTaskId("9528");
        //     task.setRcvTime(new Date().toString());
        ArrayList<String> ips = new ArrayList<String>();
        ips.add("1");
        ips.add("2");
        task.setIplist(ips);
        logger.info("createBinding");
        MessageUtil.getInstance().createQueue("w_192.168.1.6");
        MessageUtil.getInstance().createBinding("w_192.168.1.6", "w_192.168.1.6");
        logger.info("sendMessages:{}", task);
        MessageUtil.getInstance().sendMessage("w_192.168.1.6", task,null);

    }

    @Test
    public void clickhouseUtilTest_basic() {
        String sql = "create table if not exists pega_test.host_info(ip String, id String, net String,system String,create_time String,update_time String)engine=Memory";
        ClickhouseUtil.getInstance().exeSql(sql);

        Connection conn = ClickhouseUtil.getInstance().getConn();
        sql = "insert into pega_test.host_info values (?,?,?,?,?,?);";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            HostInfoClick host = new HostInfoClick();
            host.setCreate_time(new Date());
            host.setUpdate_time(new Date());
            host.setIp("1.2.3.5");
//            host.setNet(PegaEnum.Net.z);
            host.setSystem_name("test");
            host.setHost_name("test_2");
            ps.setObject(1, host.getHost_name());
            ps.setObject(2, host.getIp());
//            ps.setObject(3, host.getNet());
            ps.setObject(4, host.getSystem_name());
            ps.setObject(5, host.getCreate_time());
            ps.setObject(6, host.getUpdate_time());
            int rs = ps.executeUpdate();
            logger.info("execute update host_info by inserting element:{}", rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void clickhouseUtilTest_createTable() {
//        String sql = "create table if not exists pega_test.host_info (system_name String,ip String," +
//                "net Enum8('z'=0,'v'=1),state Enum8('Main'=0,'NotMain'=1,'Maining'=2,'Cons'=3,'Retire'=4)," +
//                "host_name String,create_time DateTime,update_time DateTime)ENGINE MergeTree() ORDER BY(system_name,net,ip)";
//        ClickhouseUtil.getInstance().exeSql(sql);
//        sql = "create table if not exists pega_test.host_state_routine(ip String,system_name String," + "" +
//                "net Enum8('z'=1,'v'=2),epoch UInt32,unavail Enum8('unavail'=0,'avail'=1),update_time DateTime)" +
//                "ENGINE MergeTree() PARTITION BY toYYYYMM(update_time) ORDER BY (epoch, system_name, net)";
        String sql = "create table if not exists pega_test.host_info (id String,system_name String,ip String," +
                "state Enum8('在维'=0,'不在维'=1,'交维中'=2,'施工中'=3,'下线'=4),host_name String,create_time DateTime,update_time DateTime,net String)" +
                "ENGINE MergeTree() ORDER BY(system_name,ip)";
        ClickhouseUtil.getInstance().exeSql(sql);
        sql = "create table if not exists pega_test.host_state_routine(ip String,system_id UInt32," +
                "epoch UInt32,avail Enum8('unavail'=0,'avail'=1),update_time DateTime)ENGINE MergeTree() PARTITION BY " +
                "toYYYYMM(update_time) ORDER BY (epoch, system_id)";
        ClickhouseUtil.getInstance().exeSql(sql);
        sql = "create table if not exists pega_test.host_state_request(ip String,taskId String," +
                "avail Enum8('unavail'=0,'avail'=1),update_time DateTime)ENGINE MergeTree() PARTITION BY " +
                "toYYYYMM(update_time) ORDER BY  taskId";
        ClickhouseUtil.getInstance().exeSql(sql);
        sql = "create table if not exists pega_test.worker_mappings(id String, state Enum8('valid'=0,'invalid'=1,'update'=2),  record_time String,count int,mappings String) ENGINE MergeTree() " +
                " PARTITION BY toYYYYMM(Cast(record_time as DateTime)) ORDER BY(Cast(record_time as DateTime),id)";
        ClickhouseUtil.getInstance().exeSql(sql);

        sql = "create table if not exists pega_test.system_mappings(id UInt64, allocated_count int,unallocated_count int,state Enum8('valid'=0,'invalid'=1,'update'=2),uptime String,mapping String)ENGINE MergeTree()" +
                "PARTITION BY toYYYYMM(Cast(uptime as DateTime)) ORDER BY(Cast(uptime as DateTime),id)";
        ClickhouseUtil.getInstance().exeSql(sql);
    }

    @Test
    public void clickhouseUtilTest_batchinsertHostInfoList() {
        //String sql = "insert into pega_test.host_info (system_name,ip,net,state,host_name,create_time,update_time) values (?,?,?,?,?,?,?)";

        ArrayList<HostInfoClick> hostInfos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            HostInfoClick hinfo = new HostInfoClick();
            hinfo.setCreate_time(new Date());
            hinfo.setHost_name("ysp"+i);
            hinfo.setIp("10.10.10.10");
//            hinfo.setNet(PegaEnum.Net.z);
            hinfo.setState(PegaEnum.State.在维);
            hinfo.setSystem_name("中文" + i);
            hinfo.setUpdate_time(new Date());
            hostInfos.add(hinfo);
        }
        StringJoiner joiner = new StringJoiner(",");
        String results = TabSerializer.addObjectsFromLists(hostInfos, joiner);
        logger.info(results);
        Long begine = System.currentTimeMillis();
        String sql = "insert into pega_test.host_info (system_name,ip,net,state,host_name,create_time,update_time)  values " + results;
        //      String sql = "insert into pega_test.host_info (system_name,ip,net,state,host_name,create_time,update_time)  values ('中文','10.10.10.10','z','Main','test','2019-04-30 16:42:26','2019-04-30 16:42:26'),('中文2`1QRUY ','10.10.10.10','z','Main','test','2019-04-30 16:42:26','2019-04-30 16:42:26')";
        ClickhouseUtil.getInstance().exeSql(sql);
        Long fin = System.currentTimeMillis();
        logger.info("execute sql using millis: {}", fin - begine);
    }

    @Test
    public void clickhouseUtilTest_batchInsertHostStateRoutineList() {
        ArrayList<HostState> hostStates = new ArrayList<>();
        long epoch = 0;
        for (int i = 0; i < 10; i++) {
            HostState state = new HostState();
            state.setIp("10.10.10." + i);
//            state.setNet(PegaEnum.Net.v);
            state.setUpdate_time(new Date());
            state.setEpoch(epoch++);
            state.setStatus(PegaEnum.Avail.unavail);
            state.setSystemId(i);
            hostStates.add(state);
        }
        StringJoiner joiner = new StringJoiner(",");
        String results = TabSerializer.addObjectsFromLists(hostStates, joiner);
        logger.info(results);
        long begine = System.currentTimeMillis();
        String sql = "insert into pega_test.host_state_routine (ip,system_name,net,epoch,unavail,update_time)  values " + results;
        ClickhouseUtil.getInstance().exeSql(sql);
        long fin = System.currentTimeMillis();
        logger.info("execute sql using millis: {}", fin - begine);
    }

    @Test
    public void clickhouseUtilTest_batchInsertHostStateRequestList() {
        ArrayList<HostState> hostStates = new ArrayList<>();
        long epoch = 0;
        for (int i = 0; i < 10; i++) {
            HostState state = new HostState();
            state.setIp("10.10.10." + i);
//            state.setNet(PegaEnum.Net.v);
            state.setUpdate_time(new Date());
            state.setTaskId(String.valueOf(epoch++));
            state.setStatus(PegaEnum.Avail.unavail);
            hostStates.add(state);
        }
        StringJoiner joiner = new StringJoiner(",");
        String results = TabSerializer.addObjectsFromLists(hostStates, joiner);
        logger.info(results);
        long begine = System.currentTimeMillis();
        String sql = "insert into pega_test.host_state_request (ip,taskId,avail,update_time)  values " + results;
        ClickhouseUtil.getInstance().exeSql(sql);
        long fin = System.currentTimeMillis();
        logger.info("execute sql using millis: {}", fin - begine);
        HostStateDao dao = SpringContextUtil.getBean(HostStateDao.class);
        logger.info("insert into clickhouse by HostStateDao putRequestHostStateList");
        dao.putRequestHostStateList(hostStates);
        logger.info("insert into clickhouse by HostStateDao putRequestHostStateList");
    }

    @Test
    public void hostInfoClickDaoTest() {
        ArrayList<HostInfoClick> hostInfos = new ArrayList<>();
        long time = new Date().getTime();
        for (int i = 0; i < 2; i++) {
            HostInfoClick hinfo = new HostInfoClick();
            hinfo.setCreate_time(new Date(time + i));
            hinfo.setHost_name("test" + i);
            hinfo.setIp("10.10.10." + i);
//            hinfo.setNet(PegaEnum.Net.z);
            hinfo.setState(PegaEnum.State.在维);
            hinfo.setSystem_name("Chinese");
            hinfo.setUpdate_time(new Date(time + i * 2000));
            hostInfos.add(hinfo);
        }
        HostInfoClickDao dao = SpringContextUtil.getBean(HostInfoClickDao.class);
        if (dao == null) {
            logger.info("HostInfoDao doesn't initialized");
            System.exit(0);
        }
        logger.info("HostInfoDao test begins...............................");
        logger.info("HostInfoDao putHostInfoList test");
        dao.putHostInfoList(hostInfos);
        logger.info("HostInfoDao getHostInfosBySystemName test");
        SystemInfoClick sys = dao.getHostInfosBySystemName("Chinese2");
        logger.info(sys.toString());
        logger.info("HostInfoDao getAllHostInfos test");
        ArrayList<SystemInfoClick> systemInfoClicks = dao.getAllHostInfos();
        Iterator<SystemInfoClick> iterator = systemInfoClicks.iterator();
        while (iterator.hasNext()) {
            logger.info(iterator.next().toString());
        }
    }

    @Deprecated
    @Test
    public void hostStateDaoTest() {
        ArrayList<HostState> routineStates = new ArrayList<>();
        ArrayList<HostState> requestStates = new ArrayList<>();
        long time = new Date().getTime();
        for (int i = 0; i < 3; i++) {
            HostState routine = new HostState();
            HostState request = new HostState();
            routine.setEpoch(i);
            routine.setStatus(PegaEnum.Avail.unavail);
//            routine.setNet(PegaEnum.Net.v);
            routine.setSystemId(i);
            routine.setIp("10.10.10." + i);
            routine.setUpdate_time(new Date(time + i * 3000));
            request.setIp("10.20.10." + i);
            request.setUpdate_time(new Date(time + i * 6000));
//            request.setNet(PegaEnum.Net.z);
            request.setStatus(PegaEnum.Avail.avail);
            request.setTaskId(String.valueOf(i));
            routineStates.add(routine);
            requestStates.add(request);
        }

        HostStateDao dao = SpringContextUtil.getBean(HostStateDao.class);
        if (dao == null) {
            logger.info("HostStateDao doesn't initiated");
            System.exit(0);
        }
        logger.info("putRoutineHostStateList");
        dao.putRoutineHostStateList(routineStates);
        logger.info("putRequestHostStateList");
        dao.putRequestHostStateList(requestStates);
        logger.info("getCurrentSystemState");
        SystemState systemState = dao.getCurrentSystemState(1, 0);
        logger.info(systemState.toString());
        logger.info("getRequestHostStates");
        ArrayList<HostState> states = dao.getRequestHostStates("1");
        logger.info((states.toString()));
    }

    @Test
    public void zookeeperUtilTest() {
        logger.info("zookeeper util test begins......................................................");
        logger.info("createPersistenceNode test:{}", ZookeeperUtil.getInstance()
                .createPersistenceNode("/systems/Chinese", "314"));
        logger.info("createEphemeralNode test:{}", ZookeeperUtil.getInstance()
                .createEphemeralNode("/controller/ha/online", "10.10.10.10"));
        logger.info("createSequentialEphemeralNode test:{}", ZookeeperUtil.getInstance()
                .createSequentialEphermalNode("/controller/ha/backup", "10.10.10.20"));
        logger.info("createSequentialPersistenceNode test:{}", ZookeeperUtil.getInstance()
                .createSequentialPersistenceNode("/systems/American", "288"));

        logger.info("checkExists test:/systems/Chinese:{}", ZookeeperUtil.getInstance().checkExists("/systems/Chinese"));
        logger.info("checkExists test:/controller/ha/online:{}", ZookeeperUtil.getInstance().checkExists("/controller/ha/online"));

        logger.info("setData test:path:/systems/Chinese,value:567");
        ZookeeperUtil.getInstance().setData("/systems/Chinese", "567");
        logger.info("getData test:/systemChinese:{}", ZookeeperUtil.getInstance().getData("/systems/Chinese"));

        logger.info("getChildren test :");
        List<String> children = ZookeeperUtil.getInstance().getChildren("/systems");
        children.forEach(System.out::println);

        logger.info("deleteNode test:/controller/ha/backup");
        ZookeeperUtil.getInstance().deleteNode("/controller/ha/backup");
        logger.info("deleteChildrenIfNeeded test:/systems");
        ZookeeperUtil.getInstance().deleteChildrenIfNeeded("/systems");

        ZookeeperUtil.getInstance().disconnect();
    }

    @Test
    public void zookeeperUtilTest_watchNode() {
        try {
            PegaNodeCacheListener listener = new PegaNodeCacheListener();
            ZookeeperUtil.getInstance().registerNodeCacheListener("/systems/Chinese", listener);

            ZookeeperUtil.getInstance().setData("/systems/Chinese", "Great!");
            ZookeeperUtil.getInstance().createEphemeralNode("/systems/Chinese", "Great wall");
            Thread.sleep(1000);
            ZookeeperUtil.getInstance().setData("/systems/Chinese", "Great wall world");
            Thread.sleep(1000);
            ZookeeperUtil.getInstance().deleteNode("/systems/Chinese");
            Thread.sleep(1000);
            listener.getNodeCache().close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ZookeeperUtil.getInstance().disconnect();
    }

    @Test
    public void zookeeperUtilTest_watchChildNode() throws IOException, InterruptedException {

        PathChildrenCache childrenCache = ZookeeperUtil.getInstance().registerPathChildListener("/systems", new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                ChildData data = event.getData();

                if (data != null) {
                    logger.info("ZookeeperUtilTest_WatchChildNode: path:{},stat:{},data:{}", data.getPath(), data.getStat(), new String(data.getData()));
                } else {
                    logger.info("ZookeeperUtilTest_WatchChildNode: ChildData is null; child node is non-exists");
                    System.exit(0);
                }

                switch (event.getType()) {
                    case CHILD_ADDED:
                        logger.info("ZookeeperUtilTest_WatchChildNode:child added");
                        List<String> list = ZookeeperUtil.getInstance().getChildren("/systems");
                        list.forEach(System.out::println);
                        break;
                    case CHILD_UPDATED:
                        logger.info("ZookeeperUtilTest_WatchChildNode:Child updated: path:{},data:{}", data.getPath(), new String(data.getData()));
                        break;
                    case CHILD_REMOVED:
                        logger.info("ZookeeperUtilTest_WatchChildNode:child removed. remaining nodes:");
                        list = ZookeeperUtil.getInstance().getChildren("/systems");
                        if (list != null)
                            list.forEach(System.out::println);
                        break;
                    case CONNECTION_LOST:
                        logger.info("ZookeeperUtilTest_WatchChildNode:connection lost");
                        break;
                    case CONNECTION_RECONNECTED:
                        logger.info("ZookeeperUtilTest_WatchChildNode:connection reconnected");
                        break;
                    case CONNECTION_SUSPENDED:
                        logger.info("ZookeeperUtilTest_WatchChildNode:connection suspended");
                        break;
                    case INITIALIZED:
                        logger.info("ZookeeperUtilTest_WatchChildNode:initialized");
                        break;
                }
            }
        });


        ZookeeperUtil.getInstance().setData("/systems/Chinese", "9527");
        ZookeeperUtil.getInstance().createPersistenceNode("/systems/Chinese", "9878");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().setData("/systems/Chinese", "9999");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().createEphemeralNode("/systems/American", "0000");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().createEphemeralNode("/systems/Chinese/East", "0001");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().deleteNode("/systems/American");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().deleteChildrenIfNeeded("/systems");
        childrenCache.close();
    }

    @Test
    public void zookeeperUtilTest_watchTreeNode() throws IOException, InterruptedException {

        TreeCache treeCache = ZookeeperUtil.getInstance().registerTreeCacheListener("/systems", 2, new TreeCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {

                ChildData data = event.getData();

                if (data != null) {
                    logger.info("zookeeperUtilTest_watchTreeNode: path:{},stat:{},data:{}", data.getPath(), data.getStat(), new String(data.getData()));
                } else {
                    logger.info("zookeeperUtilTest_watchTreeNode: ChildData is null; child node is non-exists");
                }

                switch (event.getType()) {
                    case NODE_ADDED:
                        logger.info("zookeeperUtilTest_watchTreeNode:node added");
                        List<String> list = ZookeeperUtil.getInstance().getChildren("/systems");
                        list.forEach(System.out::println);
                        break;
                    case NODE_UPDATED:
                        logger.info("zookeeperUtilTest_watchTreeNode:node updated: path:{},data:{}", data.getPath(), new String(data.getData()));
                        break;
                    case NODE_REMOVED:
                        logger.info("zookeeperUtilTest_watchTreeNode:node removed. remaining nodes:");
                        list = ZookeeperUtil.getInstance().getChildren("/systems");
                        if (list != null)
                            list.forEach(System.out::println);
                        break;
                    case CONNECTION_LOST:
                        logger.info("zookeeperUtilTest_watchTreeNode:connection lost");
                        break;
                    case CONNECTION_RECONNECTED:
                        logger.info("zookeeperUtilTest_watchTreeNode:connection reconnected");
                        break;
                    case CONNECTION_SUSPENDED:
                        logger.info("zookeeperUtilTest_watchTreeNode:connection suspended");
                        break;
                    case INITIALIZED:
                        logger.info("zookeeperUtilTest_watchTreeNode:initialized");
                        break;
                    default:
                        break;
                }
            }
        });

        ZookeeperUtil.getInstance().setData("/systems/Chinese", "9527");
        ZookeeperUtil.getInstance().createPersistenceNode("/systems/Chinese", "9878");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().setData("/systems/Chinese", "9999");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().createEphemeralNode("/systems/American", "0000");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().createEphemeralNode("/systems/Chinese/East", "0001");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().createEphemeralNode("/systems/Chinese/West", "1000");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().deleteNode("/systems/American");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().deleteChildrenIfNeeded("/systems/Chinese");
        Thread.sleep(1000);
        ZookeeperUtil.getInstance().deleteChildrenIfNeeded("/systems");
        treeCache.close();
    }

    @Test
    public void host2MysqlTest() throws InterruptedException {
        ArrayList<HostInfo> hostInfos = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < 20; i++) {
            HostInfo hinfo = new HostInfo();
            hinfo.setId((long) i);
            hinfo.setSystemId(Long.valueOf(i % 2));
            hinfo.setHost_name("test" + (i));
            hinfo.setIp("10.10.10." + (i));
//            hinfo.setNet(PegaEnum.Net.z);
            hinfo.setState(PegaEnum.State.交维中);
            hinfo.setSystem_name("中文" + i % 2);
            hinfo.setUpdate_time(formatter.format(new Date()));
            hostInfos.add(hinfo);
            Thread.sleep(1000);
        }

        logger.info("host2MysqlTest begins..................");
        HostInfoService service = SpringContextUtil.getBean(HostInfoService.class);
        logger.info("host2MysqlTest addHostInfoList");
        service.addHostInfoList(hostInfos);
        logger.info("host2MysqlTest getAllSystemInfos");
        ArrayList<SystemInfo> systemInfos = service.getAllSystemInfos();
        logger.info("systemInfos:{}", systemInfos);

        hostInfos.get(0).setState(PegaEnum.State.交维中);
        logger.info("host2MysqlTest updateHostInfo");
        service.updateHostInfo(hostInfos.get(0));

        logger.info("host2MysqlTest ");
        HostInfo host = service.getHostInfo(hostInfos.get(0).getIp());
        logger.info("updated host:{}", host);

        logger.info("host2MysqlTest deleteHostInfo");
        service.deleteHostInfo(hostInfos.get(0).getIp());

        logger.info("host2MysqlTest ");
        host = service.getHostInfo("10.10.10.9");
        logger.info("host:{}", host);

    }

    @Test
    public void systemInfo2MysqlTest() throws InterruptedException {
        ArrayList<HostInfo> hostInfos = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 1; i < 21; i++) {
            HostInfo hinfo = new HostInfo();
            hinfo.setId((long) i);
            hinfo.setSystemId(Long.valueOf(i % 2));
            hinfo.setHost_name("test" + (i));
            hinfo.setIp("10.10.10." + (i));
//            hinfo.setNet(PegaEnum.Net.z);
            hinfo.setState(PegaEnum.State.交维中);
            hinfo.setSystem_name("中文" + i % 2);
            hinfo.setDmodelName("浪潮");
            hinfo.setImportance("重要");
            hinfo.setSn(String.valueOf(10000 + i));
            hinfo.setUpdate_time(formatter.format(new Date()));
            hostInfos.add(hinfo);
            Thread.sleep(1000);
        }

        SystemInfo systemInfo0 = new SystemInfo();
        systemInfo0.setId((long) 0);
        systemInfo0.setIsmaintain(PegaEnum.State.在维);
        systemInfo0.setSystemName("中文0");
        systemInfo0.setUpdateTime(formatter.format(new Date()));
        SystemInfoService systemInfoService = SpringContextUtil.getBean(SystemInfoService.class);
        HostInfoService hostInfoService = SpringContextUtil.getBean(HostInfoService.class);
        logger.info("sytemInfo2mysqlTest begins........");
        logger.info("sytemInfo2mysqlTest:addSystem");
        systemInfoService.addSystem(systemInfo0);
        logger.info("sytemInfo2mysqlTest:HostInfoService.addHostInfoList");
        hostInfoService.addHostInfoList(hostInfos);
        logger.info("sytemInfo2mysqlTest:initializeSystemInfoByName");
        SystemInfo systemInfoByGet = systemInfoService.initializeSystemInfoByName("中文0");
        logger.info(systemInfoByGet.toString());
        logger.info("sytemInfo2mysqlTest:updateSystem");
        systemInfoByGet.setIsmaintain(PegaEnum.State.不在维);
        systemInfoService.updateSystem(systemInfoByGet);
        logger.info(systemInfoByGet.toString());
        logger.info("sytemInfo2mysqlTest:updateSystem");
        systemInfoService.updateSystem(systemInfo0);
        logger.info(systemInfo0.toString());
        logger.info("sytemInfo2mysqlTest:updateSystemName");
        systemInfoByGet = systemInfoService.getSystemInfoByName("中文0");
        systemInfoService.updateSystemName("中文0", "中文0-new");
        logger.info(systemInfoByGet.toString());
        hostInfoService.updateSystemName(systemInfoByGet.getId(), "中文0-new");
        systemInfoByGet = systemInfoService.initializeSystemInfoByName("中文0-new");
        logger.info("systeminfo from db:{}", systemInfoByGet.toString());

    }

    @Test
    public void synchronizationTest() {
        logger.info("synchronizationTest begins......");
        Synchronizer synchronizer = SpringContextUtil.getBean(Synchronizer.class);
        ArrayList<SystemInfo> systemInfos = synchronizer.syncSystemsByState("在维");
        logger.info("syncSystemsByState:{}", systemInfos);
        SystemInfo systemInfo = synchronizer.syncSystemByName("在维", "中文1");
        logger.info("syncSystemByName:{}", systemInfo);

        Master master = SpringContextUtil.getBean(Master.class);
        String[] systems = new String[]{"中文1"};
        master.init(PegaEnum.State.在维, systems);
        SystemInfoService systemInfoService = SpringContextUtil.getBean(SystemInfoService.class);
        logger.info("after master init...........");
        logger.info("initializeSystemInfoByName:中文1:{}", systemInfoService.initializeSystemInfoByName("中文1"));
        logger.info("getAllSystemInfos:{}", systemInfoService.getAllSystemInfos());
        logger.info("getSystemInfoByName:中文0:{}", systemInfoService.getSystemInfoByName("中文0"));
        logger.info("Zookeeper:getChildren:{}", ZookeeperUtil.getInstance().getChildren("/systems/"));
    }

    @Test
    public void workerMappingDao_putTest() throws InterruptedException {
        StringJoiner joiner = new StringJoiner(",");
        WorkerMappings mappings = new WorkerMappings();
        WorkerRecorder worker = new WorkerRecorder();
        worker.setId("4");
        worker.setMonitorCount(100);
        worker.setState(PegaEnum.ObjectState.valid);
        //   worker.setUptime(new Date());
//        worker.setWorkingNet(PegaConfiguration.getWorkingNet());
        ArrayList<WorkAssignment> strings = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            WorkAssignment assignment = new WorkAssignment();
            assignment.setSystemId((long) i);
            assignment.setHeader(0);
            assignment.setTrailer(100);
            strings.add(assignment);
        }
        mappings.addWorkerMapping(worker, strings);
        WorkerRecorder worker2 = new WorkerRecorder();
        worker2.setId("5");
        worker2.setMonitorCount(100);
        worker2.setState(PegaEnum.ObjectState.valid);
        //      worker2.setUptime(new Date());
//        worker2.setWorkingNet(PegaEnum.Net.z);
        ArrayList<WorkAssignment> strings2 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            WorkAssignment assignment = new WorkAssignment();
            assignment.setSystemId((long) (i + 4));
            assignment.setHeader(0);
            assignment.setTrailer(100);
            strings2.add(assignment);
        }
        worker.setRecordTime(new Date());
        worker2.setRecordTime(new Date());
        mappings.addWorkerMapping(worker2, strings2);
        WorkerRecorder worker3 = new WorkerRecorder();
        worker3.setId("6");
        worker3.setMonitorCount(100);
        worker3.setState(PegaEnum.ObjectState.valid);
        //      worker3.setUptime(new Date());
//        worker3.setWorkingNet(PegaEnum.Net.v);
        ArrayList<WorkAssignment> strings3 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            WorkAssignment assignment = new WorkAssignment();
            assignment.setSystemId((long) (i + 8));
            assignment.setHeader(0);
            assignment.setTrailer(100);
            strings3.add(assignment);
        }
        worker3.setRecordTime(new Date());
        mappings.addWorkerMapping(worker3, strings3);
        logger.info("workerMappingTest begins..................");
        String result = mappings.toTabbedString();
        logger.info("WorkerMappings to TabString:{}", result);
        logger.info("WorkerMappingsDao putWorkerMapping test");
        WorkerMappingsDao dao = SpringContextUtil.getBean(WorkerMappingsDao.class);
        dao.putWorkerMapping(mappings);

    }

    @Test
    public void masterIntialization_test() {

    }

//    @Test
//    public void workerMappingDao_getTest() {
//        WorkerMappingsDao dao = SpringContextUtil.getBean(WorkerMappingsDao.class);
//        logger.info("WorkerMappingsDao getWorkerMappingsNearest test");
//        WorkerMappings mpp = dao.getWorkerMappingsNearest(new Date(),"");
//        logger.info("workerMappings:{}", mpp.toTabbedString());
//    }

    @Test
    public void systemMappingDao_putTest() {
        SystemMappings mappings = new SystemMappings();
        ArrayList<SystemRecorder> systemRecorders = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            SystemRecorder bean = new SystemRecorder();
            bean.setId(i);
//           if(i<8)
//               bean.setNet(PegaEnum.Net.z);
//           else
//               bean.setNet(PegaEnum.Net.v);
            bean.setUptime(new Date());
            bean.setUnallocatedCount(0);
            bean.setAllocatedCount(100);
            systemRecorders.add(bean);
        }

        for (int i = 0; i < 12; i++) {
            ArrayList<SystemSegment> segments = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                SystemSegment systemSegment = new SystemSegment();
                systemSegment.setHeader(0 + j * 50);
                systemSegment.setTrailer(50 + j * 50);
                if (i < 8)
                    systemSegment.setWorkerId(String.valueOf(j));
                else
                    systemSegment.setWorkerId(String.valueOf(2 + j));
                segments.add(systemSegment);
            }
            mappings.addSystemMapping(systemRecorders.get(i), segments);
        }
        logger.info("systemMappingDao_putTest begins..............................");
        logger.info("systemmappings to tabstring test:{}", mappings.toTabbedString());

        SystemMappingsDao dao = SpringContextUtil.getBean(SystemMappingsDao.class);
        dao.putSystemMappings(mappings);
    }

//    @Test
//    public void systemMappingDao_getTest() {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try {
//            Date date = formatter.parse("2019-06-19 17:03:00");
//            SystemMappingsDao dao = SpringContextUtil.getBean(SystemMappingsDao.class);
//            logger.info("systemMappingDao_getTest begins..................................");
//            SystemMappings mappings = dao.getSystemMappingsLaterThan(date);
//            logger.info("systemmapping to tabstring:{}", mappings.toTabbedString());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void masterInitTest() {
        Master master = SpringContextUtil.getBean(Master.class);
        logger.info("masterInitTest: master.init begins....................................");
        master.init(PegaEnum.State.在维, null);
        logger.info("masterInitTest: master.init ends....................................");
//        MappingManager mappingManager=SpringContextUtil.getBean(MappingManager.class);
//        for(int i =0;i<2;i++){
//            WorkerRecorder recorder= new WorkerRecorder();
//            recorder.setMonitorCount(0);
//            recorder.setId("test"+(i+1));
//            recorder.setState(PegaEnum.ObjectState.valid);
//            mappingManager.addWorker(recorder);
//        }
//        logger.info("masterInitTest: finishes adding workerRecorders");
//        mappingManager.initAllMappings();
    }

    @Test
    public void masterCronTaskTest() {
        this.clickhouseUtilTest_createTable();
        this.masterInitTest();
        MasterCronTasks tasks = SpringContextUtil.getBean(MasterCronTasks.class);
        try {
            logger.info("masterCronTaskTest:start to sleep 30 seconds.....................");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tasks.renovateMappings();
        try {
            logger.info("masterCronTaskTest:start to sleep 30  seconds.....................");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tasks.renovateMappings();
    }

    @Test
    public void curatorTest() {
        String path = "/mapping/test1";
        boolean ifExists = ZookeeperUtil.getInstance().checkExists(path);
        logger.info("curatorTest_checkExists: path={},result={}", path, ifExists);
    }

    @Test
    public void masterCronTaskTest2() {
        this.clickhouseUtilTest_createTable();
        this.masterInitTest();
        MasterCronTasks tasks = SpringContextUtil.getBean(MasterCronTasks.class);
        try {
            logger.info("masterCronTaskTest:start to sleep 30 seconds");
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
//        tasks.renovateMappings();
//        try {
//            logger.info("masterCronTaskTest:start to sleep 30  seconds");
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        tasks.renovateMappings();
//    }

    @Test
    public void redisBatchOperationTest() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<HostState> strings3 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HostState assignment = new HostState();
            assignment.setIp("10.10.10." + i);
            assignment.setStatus(PegaEnum.Avail.avail);
            assignment.setUpdate_time(new Date());
            strings3.add(assignment);
        }
        String key = "redisBatchOperationTest";
        logger.info("redisBatchOperationTest begins................................................");
        RedisClientUtil.getInstance().batchAddList(key, strings3);
        logger.info("redisBatchOperationTest after batchAddList, cache size={}", RedisClientUtil.getInstance().getListSize(key));
        List cachedObjects = RedisClientUtil.getInstance().getListPage(key, 0, -1);
        logger.info("redisBatchOperationTest :getListPage,cachedObjects={}", cachedObjects.toString());
    }

//    @Resource
//    private JczySynchronizationService jczySynchronizationService;

    @Test
    public void feignClientTest() {
        String __SYSTERM = "system";
        String __DEVICE = "device";
        String __DEVTYPE = "刀片服务器";
     String system = "509音视频监控";
     String net="z";
     //   String system="运维实验";
     //   String system="文本监控";
        logger.info("feignClientTest begins..................................................");
//        JczySystemInfoList jczySystemInfo = jczySynchronizationService.getSystemByName( __SYSTERM,system);
//         logger.info("getSystemByName: jczySystemInfo={}",jczySystemInfo.toString());
        List<JczyDeviceInfo> deviceInfos;
//        List<JczySystemInfo> infoList = jczySynchronizationService.getSystemByName(__SYSTERM, system);
//        logger.info("getSystemByName: jczySystemInfo={}", infoList.size());
//
////        String returnValue=jczySynchronizationService.getSystemByNameInString(__SYSTERM,system);
////        logger.info("getSystemByName: jczySystemInfo={}",returnValue);
//
//        List<JczySystemInfo> jczySystemInfos = jczySynchronizationService.getSystemsByState(__SYSTERM, "在维");
//        logger.info("getSystemsByState: jczySystemInfos={}", jczySystemInfos.size());
//
//       deviceInfos = jczySynchronizationService.getHostsBySystem(__DEVICE, system, __DEVTYPE);
//        logger.info("getHostsBySystem: deviceInfos={}", deviceInfos.size());

         deviceInfos = jczySynchronizationService.getHostsBySystemAndNet(__DEVICE, net,system, __DEVTYPE);
        logger.info("getHostsBySystemAndNet: deviceInfos={}", deviceInfos.size());
    }

    @Test
    public void redisClientUtilExpireTest(){
        String key="11_16";
        long time=RedisClientUtil.getInstance().getExpire(key);
        logger.info("redisClientUtilExpireTest:time={} for key ={}",time,key);
    }

    @Autowired
    KafkaUtil kafkaUtil;
    @Test
    public void kafkaSendObjectListTest(){
        logger.info("kafkaSendObjectListTest begins......");
        for(int i=0;i<=3;i++){
            AgentException exception=new AgentException();
            exception.setCode(PegaEnum.RegistrationExceptionCode.NotFoundUuid );
            exception.setIssueId("dfadfadfadg_"+i);
            exception.setTopic("exception");
            exception.setReporter("dfaqwerqewrrt_"+i);
            exception.setTime(String.valueOf(new Date()));
            exception.setReason("test");
            logger.info("kafkaSendObjectListTest: exception instance={}", exception.toString());
            kafkaUtil.send2Kafka("exception", exception);
        }
    }
}
