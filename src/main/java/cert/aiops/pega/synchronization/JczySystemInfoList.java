package cert.aiops.pega.synchronization;

import java.io.Serializable;
import java.util.List;

public class JczySystemInfoList implements Serializable {
    List<JczySystemInfo> Device;

    @Override
    public String toString() {
        return "JczySystemInfoList{" +
                "Device=" + Device +
                '}';
    }

    public List<JczySystemInfo> getDevice() {
        return Device;
    }

    public void setDevice(List<JczySystemInfo> device) {
        this.Device = device;
    }


}