package cert.aiops.pega.bean;

import cert.aiops.pega.util.CustomJsonDateDeserializer;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@JsonFilter("PublishFilter")
@Entity
@Table(name="registered_host")
public class RegisteredHost {

    private String ip;
    private long update_time;
    @Id
    @Column(nullable = false, unique = true)
    private String hostName;
    private String id=null;
    private String channels="";

    @Override
    public String toString() {
        return "RegisteredHost{" +
                "ip='" + ip + '\'' +
                ", update_time='" + getUpdate_time()+ '\'' +
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
        return new Date(update_time);
    }

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    public void setUpdate_time(Date update_time) {
        this.update_time = update_time.getTime();
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

    public List<String> getChannels() {
        List<String> channelList= Arrays.asList(channels.substring(1,channels.length()-1).split(","));
        return channelList;
    }

    public void addChannel(String channel) {
        if(channels.contains(channel))
            return;
        if(channels=="")
            channels="[";
        else
            channels=channels.substring(0,channels.length()-1);
        if(channels.length()>1)
            channels+=","+channel+"]";
        else
            channels+=channel+"]";
        channels=channels.trim();
    }

    public void removeChannel(String channel){
        if(!channels.contains(channel))
            return;
        if(channels.isEmpty())
            return;
        String pattern=","+channel;
        if(channels.contains(pattern)) {
            channels=channels.replace(pattern, "");
            return;
        }
        pattern=channel+",";
        if(channels.contains(pattern)){
            channels=channels.replace(pattern,"");
            return;
        }
        if(channels.contains(channel))
            channels=channels.replace(channel,"");


    }
}
