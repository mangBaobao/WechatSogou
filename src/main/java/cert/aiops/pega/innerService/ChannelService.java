package cert.aiops.pega.innerService;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.dao.ChannelRepository;
import cert.aiops.pega.util.PegaEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

//@Transactional
@Service
public class ChannelService {
    @Autowired
    private ChannelRepository channelRepository;

    public void storeChannel(Channel channel){
        channelRepository.save(channel);
    }

    public void updateMembers(Long id,String members,String update_time){
        channelRepository.updateChannelMembers(id,members,update_time);
    }

    public List<Channel> loadChannelsByStatus(PegaEnum.ObjectState state){
        return channelRepository.getChannelsByStatus(state.name());
    }

    public Long getChannelLargestId(){
        return channelRepository.getMaxChannelId();
    }

    public void abortChannel(Channel channel){
        this.storeChannel(channel);
    }
}
