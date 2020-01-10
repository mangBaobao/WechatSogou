package cert.aiops.pega.controller;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.channels.ChannelManager;
import cert.aiops.pega.service.ChannelQueryService;
import cert.aiops.pega.util.IPAddrUtil;
import cert.aiops.pega.util.PegaConstant;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
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
            paramsInString+=key+":";
            paramsInString+=params.get(key)+",";
        }
        paramsInString= (String) paramsInString.subSequence(0,paramsInString.length()-1);
        return paramsInString;
    }

    @RequestMapping(value="/{channel}", method = RequestMethod.POST)
    public ResponseEntity<Channel> declareChannel(@RequestParam Map<String,String> params) throws ExecutionException, InterruptedException {
        Channel errChannel=new Channel();
        if(!params.containsKey(PegaConstant.__CHANNEL_NAME) || !params.containsKey(PegaConstant.__CHANNEL_UPDATER) || !params.containsKey(PegaConstant.__CHANNEL_NET)){

            errChannel.setDescription("declaring new channel must specify name,updater and net ");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        String net=params.get(PegaConstant.__CHANNEL_NET);
        if(!net.equals(PegaEnum.Net.z.name())&&!net.equals(PegaEnum.Net.v.name())){
            errChannel.setDescription("declaring new channel must specify correct net");
            return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
        }
        if(!params.containsKey(PegaConstant.__CHANNEL_DESCRIPTION))
            params.put(PegaConstant.__CHANNEL_DESCRIPTION,"");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime=formatter.format(new Date());
        params.put(PegaConstant.__CHANNEL_UPTIME, dateTime);
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

    @RequestMapping(value="/channel/{channelName}", method = RequestMethod.POST)
    public ResponseEntity<Channel> updateChannelAttributes(@PathVariable String channelName, @RequestParam Map<String,String> params) throws ExecutionException, InterruptedException {
        Channel errChannel=new Channel();
        if( !params.containsKey(PegaConstant.__CHANNEL_UPDATER)){
            errChannel.setDescription("updateChannelAttributes must specify updater ");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        if(channelName.equals(ChannelManager.__DEFAULT_CHANNEL)){
            errChannel.setDescription("updateChannelAttributes must not modify basic channel");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }

        if(params.containsKey(PegaConstant.__CHANNEL_NET)){
            errChannel.setDescription("updateChannelAttributes cannot modify belonging net");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        if(params.containsKey(PegaConstant.__CHANNEL_UPTIME)){
            errChannel.setDescription("updateChannelAttributes cannot modify update time");
            return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
        }
        if(params.containsKey(PegaConstant.__CHANNEL_ID)){
            errChannel.setDescription("updateChannelAttributes cannot modify channel id");
            return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
        }
        if(params.containsKey(PegaConstant.__CHANNEL_MEMBERS)){
            if(!params.containsKey(PegaConstant.__CHANNEL_MEMBERACTIONTYPE)){
                errChannel.setDescription("updateChannelAttributes cannot omit action type when updating members");
                return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
            }
            String action=params.get(PegaConstant.__CHANNEL_MEMBERACTIONTYPE);
            if(!action.equals(PegaEnum.MemberAction.reduce.name())&& !action.equals(PegaEnum.MemberAction.add.name())){
                errChannel.setDescription("updateChannelAttributes receive invalid action type when updating members");
                return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
            }
            String[] members=params.get(PegaConstant.__CHANNEL_MEMBERS).split(",");
            List<String> invalidMembers= IPAddrUtil.invalidIPAddress(members);
            if(invalidMembers.size()!=0){
                errChannel.setDescription("updateChannelAttributes receive invalid ip address ="+invalidMembers.toString());
                return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
            }
        }
        if(params.containsKey(PegaConstant.__CHANNEL_STATUS)){
            String value = params.get(PegaConstant.__CHANNEL_STATUS);
            if (!value.equals(PegaEnum.ObjectState.invalid.name()) && !value.equals(PegaEnum.ObjectState.valid.name())) {
                errChannel.setDescription("updateChannelAttributes receive invalid status");
                return new ResponseEntity<>(errChannel,HttpStatus.BAD_REQUEST);
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime=formatter.format(new Date());
        params.put(PegaConstant.__CHANNEL_UPTIME, dateTime);
        params.put(PegaConstant.__CHANNEL_NAME,channelName);
        logger.info("declareChannel: passed params:{}",deserializeParams(params));
        Future<Channel> channelFuture=channelQueryService.updateChannelAttributes(params);
        Channel channel=channelFuture.get();
        if(channel==null) {
            errChannel.setDescription("channel is not existed");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
        if(!channel.getUpdater().equals(params.get(PegaConstant.__CHANNEL_UPDATER))){
            errChannel.setDescription("channel updater is not authorized");
            return new ResponseEntity<>(errChannel, HttpStatus.BAD_REQUEST);
        }
         return new ResponseEntity<>(channel,HttpStatus.ACCEPTED);
    }


    @RequestMapping(value="/channel/{name}", method = RequestMethod.GET)
    public ResponseEntity<Channel> getChannelInfo(@PathVariable  String name) throws ExecutionException, InterruptedException {
        Future<Channel> channelFuture=channelQueryService.getChannelInfo(name);
        Channel channel=channelFuture.get();
        if(channel==null){
            channel=new Channel();
            channel.setDescription("channel is not existed");
            return new ResponseEntity<>(channel,HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(channel,HttpStatus.FOUND);
    }
}
