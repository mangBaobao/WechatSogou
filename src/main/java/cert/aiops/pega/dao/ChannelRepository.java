package cert.aiops.pega.dao;

import cert.aiops.pega.bean.Channel;
import cert.aiops.pega.bean.RegisteredHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel,Long> {

    @Query(value="select *from channel where status=?1",nativeQuery = true)
     List<Channel> getChannelsByStatus(String status);

    @Query(value="select id from channel order by id desc limit 1", nativeQuery = true)
    Long getMaxChannelId();

    @Transactional
    @Modifying
    @Query(value="update channel ch set ch.status='invalid',ch.uptime=?2 where ch.id=?1",nativeQuery = true)
    void abortChannel(Long channelId, String updateTime);

    @Transactional
    @Modifying
    @Query(value="update channel ch set ch.members=?2,ch.uptime=?3 where ch.id=?1",nativeQuery = true)
    void updateChannelMembers(Long channelId, String members, String updateTime);
}
