package cert.aiops.pega.controller;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.service.ChannelQueryService;
import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ChannelController extends  BaseController {
    private Logger logger= LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private ChannelQueryService channelQueryService;

    @RequestMapping(value="/{channel}", method = RequestMethod.POST)
    public ResponseEntity<Channel> declareChannel(@RequestParam Map<String,String> params){
        return null;
    }

    @RequestMapping(value="/channel/{members}", method = RequestMethod.POST)
    public ResponseEntity<Channel> updateChannelMembers(@RequestParam Map<String,String> params){
        return null;
    }

    @RequestMapping(value="/channel/{status}", method=RequestMethod.POST)
    public ResponseEntity<Channel> updateChannelStatus(@RequestParam Map<String,String> params){
        return null;
    }

    @RequestMapping(value="/{channel}", method = RequestMethod.GET)
    public ResponseEntity<Channel> getChannelInfo(@PathVariable(name="name") String name){
        return null;
    }
}
