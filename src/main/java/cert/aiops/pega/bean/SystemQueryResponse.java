package cert.aiops.pega.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemQueryResponse implements Serializable {
    private String systemName;
    private int page_number;
    private int page_size;
    private int totalRecords;
    private ArrayList<SinglePingState> hosts_deprecated;
    private List hosts;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public String toString() {
        return "SystemQueryResponse{" +
                "systemName='" + systemName + '\'' +
                ", page_number=" + page_number +
                ", page_size=" + page_size +
                ", totalRecords=" + totalRecords +
                ", hosts=" + hosts +
                '}';
    }

    public List getHosts() {
        return hosts;
    }

    public void setHosts(List hosts) {
        this.hosts = hosts;
    }

    public int getPage_number() {
        return page_number;
    }

    public void setPage_number(int page_number) {
        this.page_number = page_number;
    }

    public int getPage_size() {
        return page_size;
    }

    public void setPage_size(int page_size) {
        this.page_size = page_size;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

//    public ArrayList<SinglePingState> getHosts() {
//        return hosts_deprecated;
//    }
//
//    public void setHosts(ArrayList<SinglePingState> hosts_deprecated) {
//        this.hosts_deprecated = hosts_deprecated;
//    }
}
