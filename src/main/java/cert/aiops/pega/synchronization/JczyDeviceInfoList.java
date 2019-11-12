package cert.aiops.pega.synchronization;

import java.util.List;

public class JczyDeviceInfoList {

    private List<JczyDeviceInfo> Device;

    @Override
    public String toString() {
        return "JczyDeviceInfoList{" +
                "Device=" + Device +
                '}';
    }

    public List<JczyDeviceInfo> getDevice() {
        return Device;
    }

    public void setDevice(List<JczyDeviceInfo> device) {
        Device = device;
    }


}
