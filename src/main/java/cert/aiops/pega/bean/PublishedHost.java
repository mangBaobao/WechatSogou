package cert.aiops.pega.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Time;

@Entity
@Table(name="hostName")
public class PublishedHost {

    private String ip;
    private Time update_time;
    @Id
    @Column(nullable = false, unique = true)
    private String hostName;
    private String id;
    private String channels;

    @Override
    public String toString() {
        return "PublishedHost{" +
                "ip='" + ip + '\'' +
                ", update_time='" + update_time + '\'' +
                ", hostName='" + hostName + '\'' +
                ", id='" + id + '\'' +
                ", channels='" + channels + '\'' +
                '}';
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Time getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Time update_time) {
        this.update_time = update_time;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }
}
