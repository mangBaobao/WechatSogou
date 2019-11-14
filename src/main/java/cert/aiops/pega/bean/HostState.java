package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HostState implements Serializable, TabSerializable {
    private String ip;
    private long systemId;
//    private PegaEnum.Net net;
    private long epoch=-1;
    private String taskId=null;
    private PegaEnum.Avail status;
    private Date update_time;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getSystemId() {
        return systemId;
    }

    public void setSystemId(long systemId) {
        this.systemId = systemId;
    }

//    public PegaEnum.Net getNet() {
//        return net;
//    }
//
//    public void setNet(PegaEnum.Net net) {
//        this.net = net;
//    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public PegaEnum.Avail getStatus() {
        return status;
    }

    public void setStatus(PegaEnum.Avail status) {
        this.status = status;
    }

    public String getUpdate_time() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(update_time);
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    @Override
    public String toString() {
        return "HostState{" +
                "ip='" + ip + '\'' +
                ", systemId='" + systemId + '\'' +
                ", epoch=" + epoch +
                ", taskId='" + taskId + '\'' +
                ", status=" + status +
                ", update_time=" + update_time +
                '}';
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toTabbedString() {
        StringJoiner joiner = new StringJoiner(",");
        TabSerializer.addValue(ip,joiner);
        if(epoch!=-1) {
            TabSerializer.addValue(systemId, joiner);
//        TabSerializer.addValue(net,joiner);
            TabSerializer.addValue(epoch, joiner);
        }
        else
            TabSerializer.addValue(taskId,joiner);
        TabSerializer.addValue(status,joiner);
        TabSerializer.addValue(update_time,joiner);
        return joiner.toString();
    }
}
