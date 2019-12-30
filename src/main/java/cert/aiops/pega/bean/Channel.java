package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;

import javax.persistence.*;

@Entity
@Table(name="channel")
public class Channel {
    String name;
    @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String updator;
    private String update_time;
    private String members;
    @Enumerated(EnumType.STRING)
    private PegaEnum.ObjectState status;

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", updator='" + updator + '\'' +
                ", update_time='" + update_time + '\'' +
                ", members='" + members + '\'' +
                ", status=" + status +
                '}';
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

    public String getUpdator() {
        return updator;
    }

    public void setUpdator(String updator) {
        this.updator = updator;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
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
