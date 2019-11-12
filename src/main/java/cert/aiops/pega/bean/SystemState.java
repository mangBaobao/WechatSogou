package cert.aiops.pega.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class SystemState implements Serializable {

    private long systemId;
    private ArrayList<HostState>  stateList;
    private int totalRecords;

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getSystemId() {
        return systemId;
    }

    public void setSystemId(long systemId) {
        this.systemId = systemId;
    }

    public ArrayList<HostState> getStateList() {
        return stateList;
    }

    public void setStateList(ArrayList<HostState> stateList) {
        this.stateList = stateList;
    }

    @Override
    public String toString() {
        return "SystemState{" +
                "systemId='" + systemId + '\'' +
                ", stateList=" + stateList +
                ", totalRecords=" + totalRecords +
                '}';
    }
}
