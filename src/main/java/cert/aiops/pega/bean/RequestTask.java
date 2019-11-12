package cert.aiops.pega.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class RequestTask implements Serializable {
    private String taskId;
   // private String rcvTime;
    private ArrayList<String> iplist;
    //private String system_name;

    @Override
    public String toString() {
        return "RequestTask{" +
                "taskId='" + taskId + '\'' +
                ", iplist=" + iplist +
                '}';
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

//    public String getRcvTime() {
//        return rcvTime;
//    }
//
//    public void setRcvTime(String rcvTime) {
//        this.rcvTime = rcvTime;
//    }

    public ArrayList<String> getIplist() {
        return iplist;
    }

    public void setIplist(ArrayList<String> iplist) {
        this.iplist = iplist;
    }

//    public String getSystem_name() {
//        return system_name;
//    }
//
//    public void setSystem_name(String system_name) {
//        this.system_name = system_name;
//    }
}
