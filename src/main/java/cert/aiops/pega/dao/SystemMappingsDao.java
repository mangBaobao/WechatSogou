package cert.aiops.pega.dao;

import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.bean.mapping.*;
import cert.aiops.pega.util.ClickhouseUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Component
@PropertySource("classpath:utility.properties")
public class SystemMappingsDao {
    Logger logger = LoggerFactory.getLogger(SystemMappingsDao.class);

    @Value("${spring.database.clickhouse}")
    private String database;

    public void putSystemMappings(SystemMappings mappings) {
        String jsonString = mappings.toTabbedString();
        String sql = " insert into " + database + ".system_mappings(id,allocated_count,unallocated_count,state,uptime,mapping) values " + jsonString;
        ClickhouseUtil.getInstance().exeSql(sql);
    }

    public HashMap<SystemRecorder, ArrayList<SystemSegment>> getSystemMappingsLaterThan(Date queryTime,String workerId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = formatter.format(queryTime);
        SystemMappings mappings = new SystemMappings();
        HashMap<SystemRecorder, ArrayList<SystemSegment>> hashMap=new HashMap<>();
        String preSql = "select uptime,dateDiff('second',toDateTime('" + time + "'),Cast(uptime as DateTime)) as diff_time from  " + database + ".system_mappings where diff_time<=0  and id="+workerId+"  order by diff_time desc limit 1";
        try {
            ResultSet resultSet = ClickhouseUtil.getInstance().exeSql(preSql);
            String uptime = null;
            while (resultSet.next())
                uptime = resultSet.getString(1);
            if (uptime != null) {
                String sql = "select * from  " + database + ".system_mappings where uptime='" + uptime + "'"+" and id="+workerId;
                resultSet = ClickhouseUtil.getInstance().exeSql(sql);
                ObjectMapper mapper = new ObjectMapper();
                while (resultSet.next()) {
                    SystemRecorder systemRecorder = new SystemRecorder();
                    systemRecorder.setUptime(formatter.parse(resultSet.getString("uptime")));
                    systemRecorder.setId(resultSet.getLong("id"));
//                systemRecorder.setNet(PegaEnum.Net.valueOf(resultSet.getString("net")));
                    systemRecorder.setState(PegaEnum.ObjectState.valueOf(resultSet.getString("state")));
                    systemRecorder.setAllocatedCount(resultSet.getInt("allocated_count"));
                    systemRecorder.setUnallocatedCount(resultSet.getInt("unallocated_count"));
                    String mapping = resultSet.getString("mapping");
                    JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, SystemSegment.class);
                    ArrayList<SystemSegment> assignments = mapper.readValue(mapping, type);
                    mappings.addSystemMapping(systemRecorder, assignments);
                    hashMap.put(systemRecorder,assignments);
                    return hashMap;
                }
                //return mappings;

            }
        } catch (Exception e) {
            logger.error("getSystemMappingsNearest fail:{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
