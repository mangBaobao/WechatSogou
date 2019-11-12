package cert.aiops.pega.controller;

import cert.aiops.pega.aspects.RequestLimit;
import cert.aiops.pega.bean.FilterResponse;
import cert.aiops.pega.bean.RequestTaskResponse;
import cert.aiops.pega.service.FilterQueryService;
import cert.aiops.pega.startup.BeingMasterCondition;
import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@Conditional(value={BeingMasterCondition.class})
@PropertySource("classpath:application.properties")
public class FilterController extends BaseController {
    Logger logger = LoggerFactory.getLogger(FilterController.class);

    @Autowired
    private FilterQueryService filterQueryService;

    public FilterController(){
        super();
    }

    @RequestMapping(value ="/unavail", method = RequestMethod.GET)
    @RequestLimit(count=2)
    public ResponseEntity<FilterResponse>  getUnavailBySystems(){
        Date time=getNowDate();
        logger.info("FilterController_getUnavailBySystems: recieve requests from ip={} by time={}",super.getRequesterIp(),time);
        Future<FilterResponse> response = filterQueryService.getUnavailBySystems(time);
        logger.info("FilterController_getUnavailBySystems: finish process requests from ip={} by time={}",super.getRequesterIp(),time);
        try {
            return new ResponseEntity<>(response.get(), HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
