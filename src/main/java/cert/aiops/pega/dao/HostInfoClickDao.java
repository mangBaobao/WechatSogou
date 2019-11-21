package cert.aiops.pega.dao;

import cert.aiops.pega.bean.HostInfoClick;
import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.bean.SystemInfoClick;
import cert.aiops.pega.util.ClickhouseUtil;
import cert.aiops.pega.util.TabSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@PropertySource("classpath:utility.properties")
public class HostInfoClickDao {
    private Logger logger = LoggerFactory.getLogger(HostInfoClickDao.class);

    @Value("${spring.database.clickhouse}")
    private String database;

    public void putHostInfoList(ArrayList<HostInfoClick> hostInfos) {
        StringJoiner joiner = new StringJoiner(",");
        String results = TabSerializer.addObjectsFromLists(hostInfos, joiner);
//        logger.info(results);
        String sql = "insert into " + database + ".host_info (system_name,ip,state,host_name,create_time,update_time)  values " + results;
        ClickhouseUtil.getInstance().exeSql(sql);
    }

    public SystemInfoClick queryHostInfosBySystemName(String systemName) {
        String sql = "select * from " + database + ".host_info where system_name='" + systemName + "' order by update_time";
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
        SystemInfoClick systemInfoClick = new SystemInfoClick();
        systemInfoClick.setSystemName(systemName);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<HostInfoClick> hostInfos = new ArrayList<>();
        try {
            while (results.next()) {
                HostInfoClick hostInfo = new HostInfoClick();
                hostInfo.setIp(results.getString("ip"));
                hostInfo.setSystem_name(results.getString("system_name"));
                hostInfo.setCreate_time(results.getTimestamp("create_time"));
                hostInfo.setUpdate_time(results.getTimestamp("update_time"));
//                hostInfo.setNet(PegaEnum.Net.valueOf(results.getString("net")));
                hostInfo.setState(PegaEnum.State.valueOf(results.getString("state")));
                hostInfo.setHost_name(results.getString("host_name"));
                hostInfos.add(hostInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        systemInfoClick.setHosts(hostInfos);
        systemInfoClick.setHostCount(hostInfos.size());
//        systemInfoClick.setNet(hostInfos.get(0).getNet());
        try {
            systemInfoClick.setCreateTime(formatter.parse(hostInfos.get(0).getCreate_time()));
            systemInfoClick.setUpdateTime(formatter.parse(hostInfos.get(hostInfos.size() - 1).getUpdate_time()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return systemInfoClick;
    }

    public ArrayList<SystemInfoClick> getAllHostInfos() {
        String sql = "select distinct system_name from " + database + ".host_info";
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
        try {
            ArrayList<SystemInfoClick> systemInfoClicks = new ArrayList<>();
            while (results.next()) {
                String name = results.getString("system_name");
                SystemInfoClick sys = queryHostInfosBySystemName(name);
                systemInfoClicks.add(sys);
            }
            return systemInfoClicks;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
