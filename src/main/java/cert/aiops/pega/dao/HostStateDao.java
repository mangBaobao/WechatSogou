package cert.aiops.pega.dao;

import cert.aiops.pega.bean.HostState;
import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.bean.SinglePingState;
import cert.aiops.pega.bean.SystemState;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.util.ClickhouseUtil;
import cert.aiops.pega.util.SpringContextUtil;
import cert.aiops.pega.util.TabSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringJoiner;

@Component
@PropertySource("classpath:utility.properties")
public class HostStateDao {

    private Logger logger = LoggerFactory.getLogger(HostStateDao.class);

    @Value("${spring.database.clickhouse}")
    private String database;

    public void putRoutineHostStateList(ArrayList<HostState> states){
        StringJoiner joiner = new StringJoiner(",");
        String results = TabSerializer.addObjectsFromLists(states, joiner);
        String sql = "insert into " + database + ".host_state_routine (ip,system_id," +
                "net,epoch,avail,update_time)  values " + results;
        ClickhouseUtil.getInstance().exeSql(sql);

    }

    public void putRequestHostStateList(ArrayList<HostState> states){
        StringJoiner joiner = new StringJoiner(",");
        String results = TabSerializer.addObjectsFromLists(states, joiner);
        String sql = "insert into " + database + ".host_state_request(ip,taskId,avail,update_time)  values " + results;
        ClickhouseUtil.getInstance().exeSql(sql);

    }

    public SystemState getCurrentSystemState(long systemId, long epoch){
         String sql = "select distinct ip,system_id,epoch,avail,update_time from " + database + ".host_state_routine where system_id='" + systemId + "' and epoch="+epoch;
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);

        SystemState state = new SystemState();
        state.setSystemId(systemId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<HostState>  stateList = new ArrayList<>();
        try {
            while (results.next()) {
                HostState hostState = new HostState();
                hostState.setIp(results.getString("ip"));
                hostState.setEpoch(epoch);
                hostState.setSystemId(results.getInt("system_id"));
                hostState.setUpdate_time(results.getTimestamp("update_time"));
//                hostState.setNet(PegaEnum.Net.valueOf(results.getString("net")));
                hostState.setStatus(PegaEnum.Avail.valueOf(results.getString("avail")));
                stateList.add(hostState);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        state.setStateList(stateList);
        state.setTotalRecords(stateList.size());
        return state;
    }

    public ArrayList<SinglePingState> getUnavailHostStates(long systemId, long epoch) {
        String sql = "select ip,system_id,epoch,avail,update_time from " + database + ".host_state_routine where system_id=" + systemId + " and epoch=" + epoch + " and avail= 'unavail'";
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
        ArrayList<SinglePingState> stateList = new ArrayList<>();
        PegaConfiguration pegaConfiguration = SpringContextUtil.getBean(PegaConfiguration.class);
        String net=pegaConfiguration.getWorkingNet();
        try {
            while (results.next()) {
                SinglePingState hostState = new SinglePingState();
                hostState.setIp(results.getString("ip"));
//                hostState.setEpoch(epoch);
//                hostState.setSystemId(results.getInt("system_id"));
                hostState.setUpdate_time(String.valueOf(results.getTimestamp("update_time")));
//                hostState.setNet(PegaEnum.Net.valueOf(results.getString("net")));
                hostState.setStatus(PegaEnum.Avail.valueOf(results.getString("avail")));
                hostState.setNet(net);
                stateList.add(hostState);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stateList;
    }

    public ArrayList<HostState> getHostStates(long systemId, long epoch){
        String contentTable="content"+systemId+epoch;
        String baseTable="base"+systemId+epoch;
        String sql="create temporary table if not exists "+contentTable+" engine=Memory as select * from "+database+".host_state_routine where system_id=" + systemId +  " and epoch="+epoch;
        ClickhouseUtil.getInstance().exeSql(sql);
         sql="create temporary table if not exists "+baseTable+" engine=Memory as select ip as base_ip, max(update_time) as base_time from "+database+
                 ".host_state_routine where system_id=" + systemId +  " and epoch="+epoch+" group by ip";
         ClickhouseUtil.getInstance().exeSql(sql);
         sql="select ip,system_id,epoch,avail,update_time from "+contentTable+","+baseTable+" where "+contentTable+".ip="+baseTable+".base_ip and "+contentTable+".update_time="+baseTable+".base_time";
//        String sql = "select * from " + database + ".host_state_routine where update_time in (select  min(update_time) from "
//                +database+".host_state_routine where system_id=" +systemId + " and epoch="+epoch+" group by ip)";
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<HostState>  stateList = new ArrayList<>();
        try {
            while (results.next()) {
                HostState hostState = new HostState();
                hostState.setIp(results.getString("ip"));
                hostState.setEpoch(epoch);
                hostState.setSystemId(results.getInt("system_id"));
                hostState.setUpdate_time(results.getTimestamp("update_time"));
//                hostState.setNet(PegaEnum.Net.valueOf(results.getString("net")));
                hostState.setStatus(PegaEnum.Avail.valueOf(results.getString("avail")));
                stateList.add(hostState);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            sql = "drop table " + contentTable;
            ClickhouseUtil.getInstance().exeSql(sql);
            sql = "drop table " + baseTable;
            ClickhouseUtil.getInstance().exeSql(sql);
        }
      return stateList;
    }

    public ArrayList<HostState> getRequestHostStates(String taskId){
        String sql = "select * from " + database + ".host_state_request where taskId='" + taskId+"'" ;
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
        ArrayList<HostState>  stateList = new ArrayList<>();
        try {
            while (results.next()) {
                HostState hostState = new HostState();
                hostState.setIp(results.getString("ip"));
                hostState.setTaskId(results.getString("taskId"));
                hostState.setUpdate_time(results.getTimestamp("update_time"));
//                hostState.setNet(PegaEnum.Net.valueOf(results.getString("net")));
                hostState.setStatus(PegaEnum.Avail.valueOf(results.getString("avail")));
                stateList.add(hostState);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stateList;
    }
}
