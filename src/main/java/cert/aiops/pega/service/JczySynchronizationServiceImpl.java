package cert.aiops.pega.service;

import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.synchronization.JczyDeviceInfo;
import cert.aiops.pega.synchronization.JczyDeviceInfoList;
import cert.aiops.pega.synchronization.JczySystemInfo;
import cert.aiops.pega.synchronization.JczySystemInfoList;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
only used for unit test by avoiding feign
 */
@Component
@Primary
public class JczySynchronizationServiceImpl implements JczySynchronizationService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    JczyDeviceInfoList jczyDeviceInfoList;
    JczySystemInfoList jczySystemInfoList;

    public JczySynchronizationServiceImpl(){
        init();
    }

    public void init(){
        jczySystemInfoList = new JczySystemInfoList();
        ArrayList<JczySystemInfo> systemInfos = new ArrayList<>();
//        for(int i =0; i<3;i++){
            JczySystemInfo jczySystemInfo =new JczySystemInfo();
            jczySystemInfo.setFname("音视频");
            jczySystemInfo.setSname("音视频");
            jczySystemInfo.setGroups("666");
            jczySystemInfo.setId((long) 666001);
            jczySystemInfo.setIsmaintain(PegaEnum.State.在维);
            jczySystemInfo.setUtime(formatter.format(new Date()));
            systemInfos.add(jczySystemInfo);
//        }
        jczySystemInfoList.setDevice(systemInfos);
        jczyDeviceInfoList = new JczyDeviceInfoList();
        ArrayList<JczyDeviceInfo> jczyDeviceInfos=new ArrayList<>();
        int[] ip_c={150,154,22};
        int[] ip_d={40,30,90};
        for(int i=0;i<3;i++) {
            for (int j = 0; j < ip_d[i]; j++) {
                JczyDeviceInfo jczyDeviceInfo = new JczyDeviceInfo();
                jczyDeviceInfo.setSid(jczySystemInfo.getId());
                jczyDeviceInfo.setSname(jczySystemInfo.getSname());
                jczyDeviceInfo.setUtime(jczySystemInfo.getUtime());
                jczyDeviceInfo.setBnetwork_name("z");
                jczyDeviceInfo.setDevtype("服务器");
                jczyDeviceInfo.setDid(ip_c[i] +"0"+ j);
                jczyDeviceInfo.setDname("test"+i+"-"+j);
                jczyDeviceInfo.setDsn(100000+String.valueOf(j));
                jczyDeviceInfo.setDstatus("Dstatus");
                jczyDeviceInfo.setDstatus_name("在维");
                jczyDeviceInfo.setIp("10.168."+ip_c[i]+"."+j);
                jczyDeviceInfos.add(jczyDeviceInfo);
            }
        }
        jczyDeviceInfoList.setDevice(jczyDeviceInfos);
    }

    @Override
    public List<JczyDeviceInfo> getRequiredHosts(String device, String system, String state) {
        return getHostsBySystem(device,system,state);
    }

    @Override
    public List<JczyDeviceInfo> getHostsBySystem(String device, String system,String type) {
        List<JczyDeviceInfo> list = new ArrayList<>();
        ArrayList<JczyDeviceInfo> deviceInfos = new ArrayList<>();
        for(JczyDeviceInfo deviceInfo:jczyDeviceInfoList.getDevice()){
            if(deviceInfo.getSname().equals(system))
                deviceInfos.add(deviceInfo);
        }
        return list;
    }

    @Override
    public List<JczyDeviceInfo> getHostsBySystemAndNet(String device, String net, String system, String devtype) {
        return this.jczyDeviceInfoList.getDevice();
    }

    @Override
    public List<JczySystemInfo> getSystemsByState(String system, String state) {
        return this.jczySystemInfoList.getDevice();
    }

    @Override
    public List<JczySystemInfo> getSystemByName(String system, String name) {
        return this.jczySystemInfoList.getDevice();
    }

//@Override
//    public JczySystemInfoList getSystemByName(String system,String name) {
//
//        JczySystemInfoList jczySystemInfoList=new JczySystemInfoList();
//        List<JczySystemInfo> list=new ArrayList<>();
//        for(JczySystemInfo info : jczySystemInfoList.getDevice()){
//            if(info.getSname().equals(name))
//                list.add(info);
//        }
//        jczySystemInfoList.setDevice(list);
//        return jczySystemInfoList;
//    }

    @Override
    public List<JczySystemInfo> getSystemByNameInList(String system, String name) {
          return this.jczySystemInfoList.getDevice();
    }

    @Override
    public String getSystemByNameInString(String system, String name) {
        return this.jczySystemInfoList.getDevice().get(0).getSname();
    }
}
