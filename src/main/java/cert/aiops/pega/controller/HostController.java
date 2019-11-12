package cert.aiops.pega.controller;

import cert.aiops.pega.aspects.RequestLimit;
import cert.aiops.pega.bean.RequestTaskResponse;
import cert.aiops.pega.service.HostQueryService;
import cert.aiops.pega.startup.BeingMasterCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@Conditional(value={BeingMasterCondition.class})
@PropertySource("classpath:application.properties")
public class HostController  extends BaseController{
    Logger logger = LoggerFactory.getLogger(HostController.class);
    @Autowired
    HostQueryService hostQueryService;

    @Value("${pega.requestHostLimit}")
    private int maxHostCount;

    public HostController(){
        super();

    }

    @RequestMapping(value = {"/hosts/{net}"}, method = RequestMethod.GET)
    @RequestLimit(count=30)
    public ResponseEntity<RequestTaskResponse>  getHostState(@PathVariable(name = "net") String net, @RequestParam(name = "ip_list") String ipList) {
        String network;
        String[] ips;
        network = net;
        ips = ipList.split(",");
//        PegaConfiguration configuration= SpringContextUtil.getBean(PegaConfiguration.class);
//        maxHostCount=configuration.getRequestHostLimit();
        if(ips.length>maxHostCount){
            logger.info("HostController.getHostState fail. Reason=IP List length exceeds limit. IP List Length={}, limit={}",ips.length,maxHostCount);
            return null;
        }
        logger.info("pass request to hostQueryService: network={},ips={},from={}",network,ips,getRequesterIp());
        Future<RequestTaskResponse> response = hostQueryService.getHostState(network,ips,getNowDate());
        try {
            return new ResponseEntity<>(response.get(), HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = {"/{hosts}"}, method = RequestMethod.GET)
    @RequestLimit(count=30)
    public ResponseEntity<RequestTaskResponse>  getHostState(@RequestParam Map<String, String> params) {

        if (params != null) {
            String network;
            String[] ips;
            network = params.get("net");
            ips = params.get("ip_list").split(",");
//            PegaConfiguration configuration= SpringContextUtil.getBean(PegaConfiguration.class);
//            maxHostCount=configuration.getRequestHostLimit();
            if(ips.length>maxHostCount){
                logger.info("HostController.getHostState fail. Reason=IP List length exceeds limit. IP List Length={}, limit={}",ips.length,maxHostCount);
                return null;
            }

            logger.info("pass request to hostQueryService: network={},ips={}",network,ips);
           Future<RequestTaskResponse> response = hostQueryService.getHostState(network,ips,getNowDate());
            try {
                return new ResponseEntity<>(response.get(), HttpStatus.OK);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
