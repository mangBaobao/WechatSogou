package cert.aiops.pega.service;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.channels.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Future;

@Service
public class ChannelQueryService {
    Logger logger= LoggerFactory.getLogger(ChannelQueryService.class);

    @Autowired
    private ChannelManager channelManager;

    @Async("channelQueryExecutor")
    public Future<Channel> declareChannel(Map<String, String> params){
        Channel channel=channelManager.getChannelByName(params.get("name"));
        return null;

    }

}
