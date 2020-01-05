package cert.aiops.pega.channels;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.bean.RegisteredHost;
import cert.aiops.pega.innerService.ChannelService;
import cert.aiops.pega.registratedHostManagement.RegisteredHostManager;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@PropertySource("classpath:application.properties")
public class ChannelManager {

    private Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    private Long maxChannelId = Long.valueOf(1000);
    public final static String __DEFAULT_CHANNEL = "basic";
    private ArrayList<Channel> channels;
//    private ArrayList<Channel> invalidChannels;

    @Value("pega.workingNet")
    private String workingNet;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private RegisteredHostManager hostManager;

    public void init() {
        channels = (ArrayList<Channel>) channelService.loadAllChannels();
        logger.info("init: succeed to load all channel count={}", channels.size());
//        invalidChannels = (ArrayList<Channel>) channelService.loadChannelsByStatus(PegaEnum.ObjectState.invalid);
//        logger.info("init: succeed to load invalid channel count={}", invalidChannels.size());
        if(channels.isEmpty()){
            Channel channel=createBasicChannel();
            channels.add(channel);
            channelService.storeChannel(channel);
            logger.info("init: succeed to init basic channel ={}",channel.toString());
        }
        Long id = channelService.getChannelLargestId();
        if (id > maxChannelId)
            maxChannelId = id;
    }

    private Channel createBasicChannel(){
        Channel channel=new Channel();
        channel.setStatus(PegaEnum.ObjectState.valid);
        channel.setDescription("basic channel for all agents");
        channel.setWorkingNet(workingNet);
        channel.setName(this.__DEFAULT_CHANNEL);
        channel.setId(maxChannelId);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dateString=format.format(new Date());
        channel.setUptime(dateString);
        channel.setUpdater("Admin");
        channel.setMembers("");
        return channel;
    }

    public Channel getChannelById(Long id) {
        for (Channel e : channels) {
            if (e.getId() == id)
                return e;
        }
        return null;
    }

    public Channel getChannelByName(String name) {
        for (Channel e : channels) {
            if (e.getName().equals(name))
                return e;
        }
        return null;
    }

    public Long getMaxChannelId() {
        maxChannelId++;
        return maxChannelId;
    }

    public void abortChannel(Channel channel) {
        Long id = channel.getId();
        for (Channel c : channels) {
            if (c.getId() == id) {
                c.setStatus(PegaEnum.ObjectState.invalid);
                List<String> hosts=getHostIpsByChannel(c.getId());
                for(String ip:hosts){
                    RegisteredHost host=hostManager.getHostByIp(ip);
                    host.removeChannel(String.valueOf(c.getId()));
                    hostManager.markingUpdatedHost(host);
                }
            }
        }
        channelService.abortChannel(channel.getId(),channel.getUptime());
        logger.info("abortChannel: successful to abort channel id={},name={}", channel.getId(), channel.getName());
    }

    public String addChannelMembers(Long id, String members) {
        Date uptime = new Date();
        Channel channel = getChannelById(id);
        String currentMembers = channel.getMembers();
        String[] requiredMembers = members.split(",");
        ArrayList<String> validMembers = new ArrayList<>();
        for (String member : requiredMembers) {
            RegisteredHost host = hostManager.getHostByIp(member);
            if (host != null) {
                host.addChannel(String.valueOf(id));
                host.setUpdate_time(uptime);
                hostManager.markingUpdatedHost(host);
                validMembers.add(member);
            }
        }
        if (!currentMembers.isEmpty()) {
            String[] currents = currentMembers.replace("[", "").replace("]", "").split(",");
            int size = validMembers.size();
            int loop;
            for (String c : currents) {
                for (loop = 0; loop < size; loop++) {
                    if (validMembers.get(loop).equals(c))
                        break;
                }
                if (loop == size)
                    validMembers.add(c);
            }
        }
        return validMembers.toString();
    }

    public String reduceChannelMembers(Long id, String members) {
        Date uptime = new Date();
        Channel channel = getChannelById(id);
        List<String> currentMembers = Arrays.asList(channel.getMembers().replace("[", "").replace("]", "").split(","));
        String[] requiredMembers = members.split(",");

        int loop;
        for (String e : requiredMembers) {
            for (loop = 0; loop < currentMembers.size(); loop++) {
                if (e.equals(currentMembers.get(loop))) {
                    RegisteredHost host = hostManager.getHostByIp(currentMembers.get(loop));
                    if (host != null) {
                        host.removeChannel(String.valueOf(id));
                        host.setUpdate_time(uptime);
                        hostManager.markingUpdatedHost(host);
                        currentMembers.remove(loop);
                    }
                }
            }
        }
        return currentMembers.toString();
    }

    public Channel updateChannelAttributes(Channel channel) {

        channelService.storeChannel(channel);
        return channel;
    }

    public void addChannel(Channel channel) {
        Long id = channel.getId();
        for (Channel c : channels) {
            if (id == c.getId())
                channels.remove(c);
        }
        channels.add(channel);
        channelService.storeChannel(channel);
    }

    public List<String> getHostIpsByChannel(Long channelId){
        Channel channel=getChannelById(channelId);
        if(channel==null)
            return null;
        List<String> currentMembers = Arrays.asList(channel.getMembers().replace("[", "").replace("]", "").split(","));
        return currentMembers;
    }

    public  Channel getBasicChannel(){
        for(Channel c:channels){
            if(c.getName().equals(this.__DEFAULT_CHANNEL))
                return c;
        }
        return null;
    }
}
