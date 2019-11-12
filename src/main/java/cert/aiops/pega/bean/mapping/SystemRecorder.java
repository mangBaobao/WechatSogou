package cert.aiops.pega.bean.mapping;

import cert.aiops.pega.bean.PegaEnum;
import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.StringJoiner;

public class SystemRecorder implements TabSerializable, Comparable<SystemRecorder> {
    private long id;
    private int allocatedCount;
    private int unallocatedCount;
    private Date uptime;
    private PegaEnum.ObjectState state;

    public PegaEnum.ObjectState getState() {
        return state;
    }

    public void setState(PegaEnum.ObjectState state) {
        this.state = state;
    }
    //    private PegaEnum.Net net;
//
//    public PegaEnum.Net getNet() {
//        return net;
//    }
//
//    public void setNet(PegaEnum.Net net) {
//        this.net = net;
//    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAllocatedCount() {
        return allocatedCount;
    }

    public void setAllocatedCount(int allocatedCount) {
        this.allocatedCount = allocatedCount;
    }

    public int getUnallocatedCount() {
        return unallocatedCount;
    }

    public void setUnallocatedCount(int unallocatedCount) {
        this.unallocatedCount = unallocatedCount;
    }

    public Date getUptime() {
        return uptime;
    }

    public void setUptime(Date uptime) {
        this.uptime = uptime;
    }

    @Override
    public String toTabbedString() {
        StringJoiner joiner = new StringJoiner(",");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TabSerializer.addValue(id, joiner);
        TabSerializer.addValue(allocatedCount, joiner);
        TabSerializer.addValue(unallocatedCount, joiner);
        TabSerializer.addValue(state, joiner);
        if (uptime != null)
            TabSerializer.addValue(formatter.format(uptime), joiner);
        return joiner.toString();
    }

    @Override
    public int compareTo(SystemRecorder candidate) {
        return (this.getUnallocatedCount() > candidate.getUnallocatedCount() ? -1 : (this.getUnallocatedCount() == candidate.getUnallocatedCount() ? 0 : 1));
    }
}
