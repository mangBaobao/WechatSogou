package cert.aiops.pega.dao;

import cert.aiops.pega.bean.mapping.WorkAssignment;
import cert.aiops.pega.bean.mapping.WorkerMappings;
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

@Component
@PropertySource("classpath:utility.properties")
public class WorkerMappingsDao {
    private Logger logger = LoggerFactory.getLogger(WorkerMappingsDao.class);

    @Value("${spring.database.clickhouse}")
    private String database;

    public void putWorkerMapping(WorkerMappings mappings) {
        String string = mappings.toTabbedString();
        String sql = "insert into " + database + ".worker_mappings(id,state,record_time,count,mappings) values " + string;
        ClickhouseUtil.getInstance().exeSql(sql);
    }

    public ArrayList<WorkAssignment> getWorkerMappingsNearest(Date queryTime,String workerId) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        WorkerMappings mappings = new WorkerMappings();
        String preSql = "select record_time,dateDiff('second',toDateTime('" + formatter.format(queryTime) + "'),Cast(record_time as DateTime)) as diff_time from  " + database + ".worker_mappings where diff_time<=0 and id="+workerId+" order by diff_time desc limit 1";
        try {
            ResultSet resultSet = ClickhouseUtil.getInstance().exeSql(preSql);
            String record_time = null;
            while (resultSet.next())
                record_time = resultSet.getString(1);
            if (record_time != null) {
                String sql = "select * from  " + database + ".worker_mappings where record_time='" + record_time + "'"+" and id="+workerId;
                resultSet = ClickhouseUtil.getInstance().exeSql(sql);
                ObjectMapper mapper = new ObjectMapper();
                while (resultSet.next()) {
//                    WorkerRecorder workerRecorder = new WorkerRecorder();
//                    workerRecorder.setRecordTime(formatter.parse(resultSet.getString("record_time")));
//   //                 workerRecorder.setUptime(formatter.parse(resultSet.getString("uptime")));
//                    workerRecorder.setId(resultSet.getString("id"));
////                workerRecorder.setWorkingNet(PegaEnum.Net.valueOf(resultSet.getString("net")));
//                    workerRecorder.setState(PegaEnum.ObjectState.valueOf(resultSet.getString("state")));
//                    workerRecorder.setMonitorCount(resultSet.getInt("count"));
                    String mapping = resultSet.getString("mappings");
                    JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, WorkAssignment.class);
                    ArrayList<WorkAssignment> assignments = mapper.readValue(mapping, type);
                    return assignments;
//                    mappings.addWorkerMapping(workerRecorder, assignments);
                }
//                return mappings;
            }
        } catch (Exception e) {
            logger.error("WorkerMappingsDao_getWorkerMappings:{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
