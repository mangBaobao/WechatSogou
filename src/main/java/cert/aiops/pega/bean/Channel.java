package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

import javax.persistence.*;

@Entity
@Table(name="channel")
public class Channel {
    String name;
    @Id
    private Long id;
    private String updater;
    private String uptime;
    private String members;
    @Enumerated(EnumType.STRING)
    private PegaEnum.ObjectState status;
    private String description;
    private String workingNet;


    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", updater='" + updater + '\'' +
                ", uptime='" + uptime + '\'' +
                ", members='" + members + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", workingNet='" + workingNet + '\'' +
                '}';
    }

    public String getWorkingNet() {
        return workingNet;
    }

    public void setWorkingNet(String workingNet) {
        this.workingNet = workingNet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public PegaEnum.ObjectState getStatus() {
        return status;
    }

    public void setStatus(PegaEnum.ObjectState status) {
        this.status = status;
    }
}
