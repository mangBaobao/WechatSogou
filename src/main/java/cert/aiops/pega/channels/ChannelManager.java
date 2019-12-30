package cert.aiops.pega.channels;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.innerService.ChannelService;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Component
public class ChannelManager {

    private Logger logger = LoggerFactory.getLogger(ChannelManager.class);

    public final static String __DEFAULT_CHANNEL = "basic";
    private ArrayList<Channel> validChannels;
    private ArrayList<Channel> invalidChannels;

    @Autowired
    private ChannelService channelService;

    public void init(){
        validChannels = (ArrayList<Channel>) channelService.loadChannelsByStatus(PegaEnum.ObjectState.valid);
        logger.info("init: succeed to load valid channel count={}", validChannels.size());
        invalidChannels= (ArrayList<Channel>) channelService.loadChannelsByStatus(PegaEnum.ObjectState.invalid);
        logger.info("init: succeed to load invalid channel count={}", invalidChannels.size());
    }

     public Channel getChannelByName(String name){
        for(Channel e: validChannels){
            if(e.getName().equals(name))
                return e;
        }
         for(Channel e: invalidChannels){
             if(e.getName().equals(name))
                 return e;
         }
        return null;
    }

    public void abortChannel(String name){
        Channel channel=this.getChannelByName(name);
        if(channel==null)
            return;
        if(channel.getStatus()== PegaEnum.ObjectState.invalid)
            return;
        Long id=channel.getId();
        Date date=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(channel.getStatus()== PegaEnum.ObjectState.valid){
            invalidChannels.add(channel);
            validChannels.remove(channel);
        }
        channelService.abortChannel(id,formatter.format(date));
    }

    public void updateChannelMembers(String name, String members){
        Channel channel=this.getChannelByName(name);
        if(channel==null || channel.getStatus()== PegaEnum.ObjectState.invalid)
            return;
        Long id=channel.getId();
        Date date=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        channelService.updateMembers(id,members,formatter.format(date));
        channel.setMembers(members);
        channel.setUpdate_time(formatter.format(date));
    }

    public void addChannel(Channel channel){
        validChannels.add(channel);
        channelService.storeChannel(channel);
    }

}
