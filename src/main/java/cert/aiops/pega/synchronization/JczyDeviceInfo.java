package cert.aiops.pega.synchronization;

public class JczyDeviceInfo {
    private String did;
    private String dname;
    private Long sid;
    private String sname;
    private String dstatus;
    private String dstatus_name;
    private String devtype;
   
    private String dmodel_name;
   
    private String rname;
    
    private String ip_num;
    private String bnetwork;
    
    private String importance;
    private String ip;
    private String dsn;
    private String utime;
    private String bnetwork_name;

    private String Dstatus_name;

    @Override
    public String toString() {
        return "JczyDeviceInfo{" +
                "did='" + did + '\'' +
                ", dname='" + dname + '\'' +
                ", sid=" + sid +
                ", sname='" + sname + '\'' +
                ", dstatus='" + dstatus + '\'' +
                ", dstatus_name='" + dstatus_name + '\'' +
                ", devtype='" + devtype + '\'' +
                ", dmodel_name='" + dmodel_name + '\'' +
                ", rname='" + rname + '\'' +
                ", ip_num='" + ip_num + '\'' +
                ", bnetwork='" + bnetwork + '\'' +
                ", importance='" + importance + '\'' +
                ", ip='" + ip + '\'' +
                ", dsn='" + dsn + '\'' +
                ", utime='" + utime + '\'' +
                ", bnetwork_name='" + bnetwork_name + '\'' +
                ", Dstatus_name='" + Dstatus_name + '\'' +
                '}';
    }

    public String getDstatus_name() {
        return Dstatus_name;
    }

    public void setDstatus_name(String dstatus_name) {
        Dstatus_name = dstatus_name;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getDmodel_name() {
        return dmodel_name;
    }

    public void setDmodel_name(String dmodel_name) {
        this.dmodel_name = dmodel_name;
    }

    public String getDevtype() {
        return devtype;
    }

    public void setDevtype(String devtype) {
        this.devtype = devtype;
    }

    public String getDsn() {
        return dsn;
    }

    public void setDsn(String dsn) {
        this.dsn = dsn;
    }

    public String getIp() {
        return ip;
    }

   

    public String getRname() {
        return rname;
    }

    public void setRname(String rname) {
        this.rname = rname;
    }

   
    public String getIp_num() {
        return ip_num;
    }

    public void setIp_num(String ip_num) {
        this.ip_num = ip_num;
    }

    public String getBnetwork() {
        return bnetwork;
    }

    public void setBnetwork(String bnetwork) {
        this.bnetwork = bnetwork;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

  

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBnetwork_name() {
        return bnetwork_name;
    }

    public void setBnetwork_name(String bnetwork_name) {
        this.bnetwork_name = bnetwork_name;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

   

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getDstatus() {
        return dstatus;
    }

    public void setDstatus(String dstatus) {
        this.dstatus = dstatus;
    }



}