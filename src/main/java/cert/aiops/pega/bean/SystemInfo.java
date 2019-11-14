package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

import javax.persistence.*;
import java.util.ArrayList;

@Entity
@Table(name="system_info")
public class SystemInfo implements Comparable<SystemInfo>{

    @Id
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(nullable = false, unique = true)
    private String systemName;
    private String updateTime;
    private PegaEnum.State ismaintain;
    @Transient
    private ArrayList<HostInfo> hosts;

    public SystemInfo(){
        hosts=new ArrayList<>();
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public PegaEnum.State getIsmaintain(){
        return ismaintain;
    }

    public void setIsmaintain(PegaEnum.State ismaintain) {
        this.ismaintain = ismaintain;
    }

    public ArrayList<HostInfo> getHosts() {
        return hosts;
    }

    public void setHosts(ArrayList<HostInfo> hosts) {
        this.hosts = hosts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    @Transient
//    public ArrayList<HostInfo> getHostsByNet(PegaEnum.Net net){
//        ArrayList<HostInfo> hostInfos = new ArrayList<>();
//        for(HostInfo host:this.hosts){
//            if(host.getNet()==net)
//                hostInfos.add(host);
//        }
//        return hostInfos;
//    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "id=" + id +
                ", systemName='" + systemName + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", ismaintain='" + ismaintain + '\'' +
                ", hosts=" + hosts +
                '}';
    }

    @Override
    public int compareTo(SystemInfo candidate) {
        return (this.getId()<candidate.getId()?-1:(this.getId()==candidate.getId()?0:1));
    }

    @Transient
    public int getHostCount(long head,long tail){
        return (int) hosts.stream().filter(o->o.getId()>=head).filter(o->o.getId()<=tail).count();
    }

    @Transient
    public HostInfo getHostInfo(HostInfo origin, int distance){
        int index=hosts.indexOf(origin);
        if(index==-1)
            return null;
        if(distance<=0)
            return origin;
        if(index+distance>=hosts.size()){
            return null;
        }
        return hosts.get(index+distance);
    }

    @Transient
    public HostInfo getHostInfo(long hostId, int distance){
        HostInfo info=getHostInfoById(hostId);
        return getHostInfo(info,distance);
    }

    @Transient
    public HostInfo getHostInfoById(long id){
        for(HostInfo host:hosts){
            if(host.getId()==id)
                return host;
        }
        return null;
    }

    @Transient
    public int getHostCount(){
        return hosts.size();
    }

    @Transient
    public ArrayList<HostInfo> getHosts(long beginId,long endId){
        ArrayList<HostInfo> hostInfos=new ArrayList<>();
        if(beginId>=endId){
            HostInfo hostInfo=this.getHostInfoById(beginId);
            hostInfos.add(hostInfo);
            return hostInfos;
        }

        for(HostInfo host:hosts){
            if(host.getId()>=beginId&&host.getId()<=endId)
                hostInfos.add(host);
        }
        return hostInfos;
    }
}
