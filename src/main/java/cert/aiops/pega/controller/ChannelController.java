package cert.aiops.pega.controller;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.service.ChannelQueryService;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class ChannelController extends  BaseController {
    private Logger logger= LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private ChannelQueryService channelQueryService;

    private String deserializeParams(Map<String,String> params){
        String paramsInString="";
        Set<String> keys=params.keySet();
        Iterator<String> iterator=keys.iterator();
        while(iterator.hasNext()){
            String key=iterator.next();
            paramsInString.concat(key+":");
            paramsInString.concat(params.get(key)+",");
        }
        paramsInString.subSequence(0,paramsInString.length()-1);
        return paramsInString;
    }

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
        if(!params.containsKey("description"))
            params.put("description","");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime=formatter.format(new Date());
        params.put("date", dateTime);
        logger.info("declareChannel: passed params:{}",deserializeParams(params));
        Future<Channel> channelFuture=channelQueryService.declareChannel(params);

        Channel channel=channelFuture.get();
        if(channel==null) {
            errChannel.setDescription("channel in future returns null");
            return new ResponseEntity<>(errChannel, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(channel.getStatus()== PegaEnum.ObjectState.invalid)
            return new ResponseEntity<>(channel,HttpStatus.CONFLICT);
        if(!channel.getUptime().equals(dateTime))
            return new ResponseEntity<>(channel,HttpStatus.ALREADY_REPORTED);
        else
            return new ResponseEntity<>(channel,HttpStatus.CREATED);
    }

    @RequestMapping(value="/channel/{name}", method = RequestMethod.POST)
    public ResponseEntity<Channel> updateChannelAttributes(@PathVariable String channelName, @RequestParam Map<String,String> params){
        Channel errChannel=new Channel();
        if( !params.containsKey("updater")){
            errChannel.setDescription("updateChannelAttributes must specify updater ");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        if(params.containsKey("net")){
            errChannel.setDescription("updateChannelAttributes cannot modify belonging net");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        if(params.containsKey("date")){
            errChannel.setDescription("updateChannelAttributes cannot modify update time");
            return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
        }
        if(params.containsKey("id")){
            errChannel.setDescription("updateChannelAttributes cannot modify channel id");
            return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime=formatter.format(new Date());
        params.put("date", dateTime);
        logger.info("declareChannel: passed params:{}",deserializeParams(params));
        Future<Channel> channelFuture=channelQueryService.updateChannelAttributes(params);
        return null;
    }

    @RequestMapping(value="/channel/{attributes}", method=RequestMethod.POST)
    public ResponseEntity<Channel> updateChannelAttributes(@RequestParam Map<String,String> params){
        return null;
    }

    @RequestMapping(value="/{channel}", method = RequestMethod.GET)
    public ResponseEntity<Channel> getChannelInfo(@PathVariable(name="name") String name){
        return null;
    }
}
