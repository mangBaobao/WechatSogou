package cert.aiops.pega.controller;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.service.ChannelQueryService;
import cert.aiops.pega.util.PegaEnum;
import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class ChannelController extends  BaseController {
    private Logger logger= LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private ChannelQueryService channelQueryService;

    @RequestMapping(value="/{channel}", method = RequestMethod.POST)
    public ResponseEntity<Channel> declareChannel(@RequestParam Map<String,String> params) throws ExecutionException, InterruptedException {
        Channel errChannel=new Channel();
        if(!params.containsKey("name") || !params.containsKey("updater") || !params.containsKey("net")){

            errChannel.setDescription("declaring new channel must specify name,updater and net ");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        String net=params.get("net");
        if(!net.equals(PegaEnum.Net.z.name())||!net.equals(PegaEnum.Net.v.name())){
            errChannel.setDescription("declaring new channel must specify correct net");
            return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime=formatter.format(new Date());
        params.put("date", dateTime);
        Future<Channel> channelFuture=channelQueryService.declareChannel(params);
        if(channelFuture==null) {
            errChannel.setDescription("future channel returns null");
            return new ResponseEntity<>(errChannel, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Channel channel=channelFuture.get();
        if(channel.getStatus()== PegaEnum.ObjectState.invalid)
            return new ResponseEntity<>(channel,HttpStatus.CONFLICT);
        if(!channel.getUpdate_time().equals(dateTime))
            return new ResponseEntity<>(channel,HttpStatus.ALREADY_REPORTED);
        else
            return new ResponseEntity<>(channel,HttpStatus.CREATED);
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
