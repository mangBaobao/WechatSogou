package cert.aiops.pega.bean;

import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.util.SpringContextUtil;

import java.io.Serializable;

public class SinglePingState  implements Serializable {
    private String net;
    private String ip;
    private PegaEnum.Avail status;
    private String update_time;
    private static final long serialVersionUID=4125096758372084309L;

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getStatus() {
        return status.ordinal();
    }

    public void setStatus(PegaEnum.Avail status) {
        this.status = status;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

}
