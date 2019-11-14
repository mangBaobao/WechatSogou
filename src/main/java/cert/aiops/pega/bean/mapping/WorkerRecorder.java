package cert.aiops.pega.bean.mapping;

import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

/**
 * used in Master node to record worker status
 */
public class WorkerRecorder implements TabSerializable,Comparable<WorkerRecorder> {
    @Override
    public String toTabbedString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringJoiner joiner = new StringJoiner(",");
        TabSerializer.addValue(id,joiner);
//        TabSerializer.addValue(workingNet,joiner);
        TabSerializer.addValue(state,joiner);
//        TabSerializer.addValue(formatter.format(uptime),joiner);
        if(recordTime!=null)
        TabSerializer.addValue(formatter.format(recordTime),joiner);
        TabSerializer.addValue(monitorCount,joiner);
        return joiner.toString();
    }

//    private PegaEnum.Net workingNet;
    private PegaEnum.ObjectState state;
    private String id;
    private Date recordTime;
    private int monitorCount=0;

    public int getMonitorCount() {
        return monitorCount;
    }

    public void setMonitorCount(int monitorCount) {
        this.monitorCount = monitorCount;
    }

    Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

//    public PegaEnum.Net getWorkingNet() {
//        return workingNet;
//    }
//
//    public void setWorkingNet(PegaEnum.Net workingNet) {
//        this.workingNet = workingNet;
//    }

    public PegaEnum.ObjectState getState() {
        return state;
    }

    public void setState(PegaEnum.ObjectState state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "WorkerRecorder{" +
                "state=" + state +
                ", id='" + id + '\'' +
                ", recordTime=" + recordTime +
                ", monitorCount=" + monitorCount +
                '}';
    }

    @Override
    public int compareTo(WorkerRecorder candidate) {
        return (this.getMonitorCount()<candidate.getMonitorCount()?-1:(this.getMonitorCount()==candidate.getMonitorCount()?0:1));
    }
}
