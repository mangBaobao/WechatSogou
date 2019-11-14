package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

import javax.persistence.*;

@Entity
@Table(name="host_info")
public class HostInfo implements  Comparable<HostInfo> {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String host_name="";
    @Column(nullable = false)
    private String ip="";
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private PegaEnum.Net net;
    @Column(nullable = false)
    private String system_name ="";
    private Long systemId;
    private String update_time;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PegaEnum.State state;
    private String dmodelName;
    private String sn;
    private String importance;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getHost_name() {
        return host_name;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public PegaEnum.State getState() {
        return state;
    }

    public void setState(PegaEnum.State state) {
        this.state = state;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

//    public PegaEnum.Net getNet() {
//        return net;
//    }

//    public void setNet(PegaEnum.Net net) {
//        this.net = net;
//    }

    public String getSystem_name() {
        return system_name;
    }

    public void setSystem_name(String system_name) {
        this.system_name = system_name;
    }

    public Long getSystemId() {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        return formatter.format(systemId);
        return systemId;
    }

    @Override
    public String toString() {
        return "HostInfo{" +
                "id=" + id +
                ", host_name='" + host_name + '\'' +
                ", ip='" + ip + '\'' +
                ", system_name='" + system_name + '\'' +
                ", systemId='" + systemId + '\'' +
                ", update_time='" + update_time + '\'' +
                ", state=" + state +
                ", dmodelName='" + dmodelName + '\'' +
                ", sn='" + sn + '\'' +
                ", importance='" + importance + '\'' +
                '}';
    }

    public String getDmodelName() {
        return dmodelName;
    }

    public void setDmodelName(String dmodelName) {
        this.dmodelName = dmodelName;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

    @Override
    public int compareTo(HostInfo candidate) {
        return (this.getId()<candidate.getId()?-1:(this.getId()==candidate.getId()?0:1));
    }
}
