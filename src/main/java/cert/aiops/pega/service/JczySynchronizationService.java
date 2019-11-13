package cert.aiops.pega.service;

import cert.aiops.pega.config.FeignClientConfigurer;
import cert.aiops.pega.synchronization.JczyDeviceInfo;
import cert.aiops.pega.synchronization.JczyDeviceInfoList;
import cert.aiops.pega.synchronization.JczySystemInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
//@PropertySource("classpath:application.properties")
////@FeignClient(name="jczy",url="http://10.40.72.141:8083",path="/jczy",configuration = FeignClientConfigurer.class)
//@FeignClient(name="jczy",url="${pega.feignclient.url}",path="/jczy",configuration = FeignClientConfigurer.class)
public interface JczySynchronizationService {
    @GetMapping("/{device}")
    List<JczyDeviceInfo> getRequiredHosts(@PathVariable("device")String device, @RequestParam("system")String system, @RequestParam("state")String state);
    @GetMapping("/{device}")
    List<JczyDeviceInfo> getHostsBySystem(@PathVariable("device")String device, @RequestParam("system")String system,@RequestParam("devtype")String devtype);
    @GetMapping("/{device}")
    List<JczyDeviceInfo> getHostsBySystemAndNet(@PathVariable("device")String device, @RequestParam("net")String net,@RequestParam("system")String system,@RequestParam("devtype")String devtype);
    @GetMapping("/{system}")
    List<JczySystemInfo> getSystemsByState(@PathVariable("system")String system, @RequestParam("state")String state);
    @GetMapping("/{system}")
    List<JczySystemInfo>  getSystemByName(@PathVariable("system")String system,@RequestParam("sname")String name);

    @GetMapping("/{system}")
    List<JczySystemInfo>  getSystemByNameInList(@PathVariable("system")String system,@RequestParam("sname")String name);

    @GetMapping("/{system}")
    String  getSystemByNameInString(@PathVariable("system")String system,@RequestParam("sname")String name);
}
