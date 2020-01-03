package cert.aiops.pega.channels;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.bean.RegisteredHost;
import cert.aiops.pega.innerService.ChannelService;
import cert.aiops.pega.registratedHostManagement.RegisteredHostManager;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class ChannelManager {

    private Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    private Long maxChannelId = Long.valueOf(1000);
    public final static String __DEFAULT_CHANNEL = "basic";
    private ArrayList<Channel> validChannels;
    private ArrayList<Channel> invalidChannels;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private RegisteredHostManager hostManager;

    public void init() {
        validChannels = (ArrayList<Channel>) channelService.loadChannelsByStatus(PegaEnum.ObjectState.valid);
        logger.info("init: succeed to load valid channel count={}", validChannels.size());
        invalidChannels = (ArrayList<Channel>) channelService.loadChannelsByStatus(PegaEnum.ObjectState.invalid);
        logger.info("init: succeed to load invalid channel count={}", invalidChannels.size());
        Long id = channelService.getChannelLargestId();
        if (id > maxChannelId)
            maxChannelId = id;
    }

    public Channel getChannelById(Long id) {
        for (Channel e : validChannels) {
            if (e.getId() == id)
                return e;
        }
        for (Channel e : invalidChannels) {
            if (e.getId() == id)
                return e;
        }
        return null;
    }

    public Channel getChannelByName(String name) {
        for (Channel e : validChannels) {
            if (e.getName().equals(name))
                return e;
        }
        for (Channel e : invalidChannels) {
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
        for (Channel c : validChannels) {
            if (c.getId() == id) {
                validChannels.remove(c);
            }
        }
        for (Channel c : invalidChannels) {
            if (c.getId() == id)
                invalidChannels.remove(c);
        }
        invalidChannels.add(channel);
        channelService.abortChannel(channel);
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
        Long id = channel.getId();
        for (Channel c : validChannels)
            if (id == c.getId())
                validChannels.remove(c);
        for (Channel c : invalidChannels)
            if (id == c.getId())
                invalidChannels.remove(c);
        if (channel.getStatus().equals(PegaEnum.ObjectState.valid))
            validChannels.add(channel);
        else
            invalidChannels.add(channel);
        channelService.storeChannel(channel);
        return channel;
    }

    public void addChannel(Channel channel) {
        Long id = channel.getId();
        for (Channel c : validChannels) {
            if (id == c.getId())
                validChannels.remove(c);
        }
        validChannels.add(channel);
        channelService.storeChannel(channel);
    }

}
