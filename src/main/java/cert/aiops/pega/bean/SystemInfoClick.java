package cert.aiops.pega.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SystemInfoClick {

    private String systemName;
    private Date updateTime;
    private Date createTime;
    private ArrayList<HostInfoClick> hosts;
    private int hostCount;
//    private PegaEnum.Net net;

//    public PegaEnum.Net getNet() {
//        return net;
//    }
//
//    public void setNet(PegaEnum.Net net) {
//        this.net = net;
//    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getUpdateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(updateTime);
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(createTime);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public ArrayList<HostInfoClick> getHosts() {
        return hosts;
    }

    public void setHosts(ArrayList<HostInfoClick> hosts) {
        this.hosts = hosts;
    }

    public int getHostCount() {
        return hostCount;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    @Override
    public String toString() {
        return "SystemInfoClick{" +
                "systemName='" + systemName + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", hosts=" + hosts +
                ", hostCount=" + hostCount +
                '}';
    }
}
