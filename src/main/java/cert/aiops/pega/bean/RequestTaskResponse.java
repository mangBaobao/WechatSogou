package cert.aiops.pega.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class RequestTaskResponse implements Serializable {
    private ArrayList<SinglePingState> hosts;

    public ArrayList<SinglePingState> getHosts() {
        return hosts;
    }

    public void setHosts(ArrayList<SinglePingState> hosts) {
        this.hosts = hosts;
    }
}
