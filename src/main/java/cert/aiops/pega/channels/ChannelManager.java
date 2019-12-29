package cert.aiops.pega.channels;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.innerService.ChannelService;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;

@Component
public class ChannelManager {

    private Logger logger = LoggerFactory.getLogger(ChannelManager.class);

    public final static String __DEFAULT_CHANNEL = "basic";
    private ArrayList<Channel> channels;

    @Autowired
    private ChannelService channelService;

    public void init(){
        channels= (ArrayList<Channel>) channelService.loadValidChannels();
    }

     Channel getChannelByName(String name){
        for(Channel e:channels){
            if(e.getName().equals(name))
                return e;
        }
        return null;
    }

    public void abortChannel(String name){
        Channel channel=this.getChannelByName(name);
        if(channel==null)
            return;
        Long id=channel.getId();
        Date date=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(channel.getStatus()== PegaEnum.ObjectState.valid){
            channels.remove(channel);
        }
        channelService.abortChannel(id,formatter.format(date));
    }

    public void updateChannelMembers(String name, String members){
        Channel channel=this.getChannelByName(name);
        if(channel==null)
            return;
        Long id=channel.getId();
        Date date=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        channelService.updateMembers(id,members,formatter.format(date));
        channel.setMembers(members);
        channel.setUpdate_time(formatter.format(date));
    }

    public void addChannel(Channel channel){
        channels.add(channel);
        channelService.storeChannel(channel);
    }

}
