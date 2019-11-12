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
                ", dsn='" + dsn + '\'' +
                ", ip='" + ip + '\'' +
                ", bnetwork_name='" + bnetwork_name + '\'' +
                ", utime='" + utime + '\'' +                
                ", rname='" + rname + '\'' +               
                ", ip_num='" + ip_num + '\'' +
                ", bnetwork='" + bnetwork + '\'' +
                ", importance='" + importance + '\'' +

                '}';
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