package cert.aiops.pega.service;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.channels.ChannelManager;
import cert.aiops.pega.util.PegaConstant;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Future;

@Service
public class ChannelQueryService {
    Logger logger = LoggerFactory.getLogger(ChannelQueryService.class);

    @Autowired
    private ChannelManager channelManager;

    @Async("channelQueryExecutor")
    public Future<Channel> declareChannel(Map<String, String> params) {
        Channel channel = channelManager.getChannelByName(params.get(PegaConstant.__CHANNEL_NAME));
        if (channel != null) {
            logger.info("declareChannel: find existed channel={}", channel.toString());
            if (channel.getStatus() == PegaEnum.ObjectState.invalid)
                return new AsyncResult<>(channel);
            else {
                channel.setUptime(params.get(PegaConstant.__CHANNEL_UPTIME));
                return new AsyncResult<>(channel);
            }
        } else {
            channel = new Channel();
            channel.setUptime(params.get(PegaConstant.__CHANNEL_UPTIME));
            channel.setName(params.get(PegaConstant.__CHANNEL_NAME));
            channel.setUpdater(params.get(PegaConstant.__CHANNEL_UPDATER));
            channel.setWorkingNet(params.get(PegaConstant.__CHANNEL_NET));
            channel.setDescription(params.get(PegaConstant.__CHANNEL_DESCRIPTION));
            channel.setId(channelManager.getMaxChannelId());
            channelManager.addChannel(channel);
            logger.info("declareChannel: new channel is created and managed ={}", channel.toString());
            return new AsyncResult<>(channel);
        }
    }

    @Async("channelQueryExecutor")
    public Future<Channel> getChannelInfo(String name){
        Channel channel=channelManager.getChannelByName(name);
        if(channel==null){
            logger.info("getChannelInfo: channel name={} is not existed",name);
            return new AsyncResult<>(null);
        }
        else
            return new AsyncResult<>(channel);
    }

    @Async("channelQueryExecutor")
    public Future<Channel> updateChannelAttributes(Map<String, String> params) {
        boolean isUpdated = false;
        Channel channel = channelManager.getChannelByName(params.get(PegaConstant.__CHANNEL_NAME));
        if (channel == null) {
            logger.info("updateChannelAttributes: channel name ={} is not existed", params.get(PegaConstant.__CHANNEL_NAME));
            return new AsyncResult<>(null);
        }
        if (!channel.getUpdater().equals(params.get(PegaConstant.__CHANNEL_UPDATER))) {
            logger.info("updateChannelAttributes: updater={} is not authorized to channel name ={} by creater={} ", params.get(PegaConstant.__CHANNEL_UPDATER),
                    params.get(PegaConstant.__CHANNEL_NAME), channel.getUpdater());
            return new AsyncResult<>(channel);
        }
        String value = params.get(PegaConstant.__CHANNEL_NEW_NAME);
        if (value != null) {
            logger.info("updateChannelAttributes: channel name={} updates to new name={}", channel.getName(), value);
            channel.setName(value);
        }
        value = params.get(PegaConstant.__CHANNEL_DESCRIPTION);
        if (value != null) {
            logger.info("updateChannelAttributes: channel description={} updates to new description={}", channel.getDescription(), value);
            channel.setDescription(value);
            isUpdated = true;
        }
        value = params.get(PegaConstant.__CHANNEL_MEMBERS);
        if (value != null) {
            String action = params.get(PegaConstant.__CHANNEL_MEMBERACTIONTYPE);
            if (action.equals(PegaEnum.MemberAction.add.name())) {
                channel.setMembers(channelManager.addChannelMembers(channel.getId(), value));
                isUpdated = true;
            } else {
             channel.setMembers(channelManager.reduceChannelMembers(channel.getId(), value));
                isUpdated = true;
            }
        }
        value = params.get(PegaConstant.__CHANNEL_UPTIME);
        channel.setUptime(value);
        value = params.get(PegaConstant.__CHANNEL_STATUS);
        if (value != null) {
                if (value.equals(PegaEnum.ObjectState.invalid.name()) && !channel.getStatus().name().equals(value)) {
                    logger.info("updateChannelAttributes: channel name={} becomes invalid", channel.getName());
                    channel.setStatus(PegaEnum.ObjectState.valueOf(value));
                    channelManager.abortChannel(channel);
                }
                else if (value.equals(PegaEnum.ObjectState.valid.name()) && !channel.getStatus().name().equals(value)) {
                    logger.info("updateChannelAttributes: channel name={} becomes valid", channel.getName());
                    channel.setStatus(PegaEnum.ObjectState.valueOf(value));
                    channelManager.addChannel(channel);
                }
                else
                    logger.info("updateChannelAttributes: channel name={} status ={} is not changed", channel.getName(), value);
            }
            if(isUpdated==true)//if status is not changed
                channelManager.updateChannelAttributes(channel);
        return new AsyncResult<>(channel);
    }
}
