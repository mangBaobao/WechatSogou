package cert.aiops.pega.service;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.channels.ChannelManager;
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
    Logger logger= LoggerFactory.getLogger(ChannelQueryService.class);

    @Autowired
    private ChannelManager channelManager;

    @Async("channelQueryExecutor")
    public Future<Channel> declareChannel(Map<String, String> params)  {
        Channel channel=channelManager.getChannelByName(params.get("name"));
        if(channel!=null){
            if(channel.getStatus()== PegaEnum.ObjectState.invalid)
                return new AsyncResult<>(channel);
            else {
                channel.setUptime(params.get("date"));
                return new AsyncResult<>(channel);
            }
        }
        else{
            channel = new Channel();
            channel.setUptime(params.get("date"));
            channel.setName(params.get("name"));
            channel.setUpdater(params.get("updater"));
            channel.setWorkingNet(params.get("net"));
            channel.setDescription(params.get("description"));
            channel.setId(channelManager.getMaxChannelId());
            channelManager.addChannel(channel);
            return new AsyncResult<>(channel);
        }
    }

    @Async("channelQueryExecutor")
    public Future<Channel> updateChannelAttributes(Map<String,String> params){
        return null;
    }
}
