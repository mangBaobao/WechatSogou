package cert.aiops.pega.bean;

import com.fasterxml.jackson.annotation.JsonFilter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;

@JsonFilter("PublishFilter")
@Entity
@Table(name="registered_host")
public class RegisteredHost {

    private String ip;
    private Date update_time;
    @Id
    @Column(nullable = false, unique = true)
    private String hostName;
    private String id=null;
    private ArrayList<String> channels=new ArrayList<>();

    @Override
    public String toString() {
        return "RegisteredHost{" +
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

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
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

    public ArrayList<String>  getChannels() {
        return channels;
    }

    public void addChannel(String channel) {
        this.channels.add(channel);
    }
}
