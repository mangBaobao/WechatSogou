package cert.aiops.pega.synchronization;

import cert.aiops.pega.bean.PegaEnum;

import java.io.Serializable;

public class JczySystemInfo implements Serializable {
    private Long id;
    private String sname;
    private String fname;
    private String scode;
    private String groups;
    private PegaEnum.State ismaintain;
    private String utime;
    
    @Override
    public String toString() {
        return "JczySystemInfo{" +
                "id=" + id +
                ", sname='" + sname + '\'' +
                ", fname='" + fname + '\'' +
                ", scode='" + scode + '\'' +
                ", groups='" + groups + '\'' +
                ", ismaintain=" + ismaintain +
                ", utime='" + utime + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }
    
    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public PegaEnum.State getIsmaintain() {
        return ismaintain;
    }

    public void setIsmaintain(PegaEnum.State ismaintain) {
        this.ismaintain = ismaintain;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }
}