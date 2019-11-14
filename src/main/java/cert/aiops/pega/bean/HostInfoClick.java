package cert.aiops.pega.bean;

import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.util.TabSerializable;
import cert.aiops.pega.util.TabSerializer;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

public class HostInfoClick implements TabSerializable {

    private String host_name="";
    private String ip="";
//    @Enumerated(EnumType.STRING)
//    private PegaEnum.Net net;
    private String system_name ="";
    private Date create_time;
    private Date update_time;
    @Enumerated(EnumType.STRING)
    private PegaEnum.State state;

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
//
//    public void setNet(PegaEnum.Net net) {
//        this.net = net;
//    }

    public String getSystem_name() {
        return system_name;
    }

    public void setSystem_name(String system_name) {
        this.system_name = system_name;
    }

    public String getCreate_time() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(create_time);
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(update_time);
    }

    public String getHost_name() {
        return host_name;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    @Override
    public String toTabbedString() {
        StringJoiner joiner = new StringJoiner(",");
        TabSerializer.addValue(system_name,joiner);
        TabSerializer.addValue(ip,joiner);
//        TabSerializer.addValue(net,joiner);
        TabSerializer.addValue(state,joiner);
        TabSerializer.addValue(host_name,joiner);
        TabSerializer.addValue(create_time,joiner);
        TabSerializer.addValue(update_time,joiner);
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "HostInfoClick{" +
                "host_name='" + host_name + '\'' +
                ", ip='" + ip + '\'' +
                ", system_name='" + system_name + '\'' +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", state=" + state +
                '}';
    }
}
