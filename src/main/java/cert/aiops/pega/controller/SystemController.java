package cert.aiops.pega.controller;

import cert.aiops.pega.aspects.RequestLimit;
import cert.aiops.pega.bean.HostState;
import cert.aiops.pega.config.PegaConfiguration;
import cert.aiops.pega.bean.SystemQueryResponse;
import cert.aiops.pega.service.SystemQueryService;
import cert.aiops.pega.startup.BeingMasterCondition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@Conditional(value={BeingMasterCondition.class})
public class SystemController extends  BaseController{

    Logger logger = LoggerFactory.getLogger(SystemController.class);
    @Autowired
    SystemQueryService systemQueryService;

    @Autowired
    PegaConfiguration pegaConfiguration;

    @RequestMapping(value={"/systems"},method = RequestMethod.GET)
    @RequestLimit(count=2)
    public ResponseEntity<SystemQueryResponse> getSystemState(@RequestParam Map<String,String> params){
        String systemName = params.get("system_name");
        int pageNumber,pageSize;
        if(params.containsKey("page_number"))
            pageNumber= Integer.parseInt(params.get("page_number"));
        else
            pageNumber=pegaConfiguration.getPageNumber();
        if(params.containsKey("page_size"))
            pageSize = Integer.parseInt(params.get("page_size"));
        else
//            pageSize=9999999;
            pageSize=pegaConfiguration.getPageSize();
        logger.info("pass request to service: systemName={},pageNumber={},page_size={},from={}",systemName,pageNumber,pageSize,getRequesterIp());
        Future<SystemQueryResponse> response = systemQueryService.getSystemState(systemName,pageNumber,pageSize,getNowDate());
        try {
            return new ResponseEntity<>(response. get(), HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
