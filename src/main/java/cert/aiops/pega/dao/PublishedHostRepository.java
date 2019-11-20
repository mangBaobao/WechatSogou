package cert.aiops.pega.dao;


import cert.aiops.pega.bean.PublishedHost;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.List;

@Repository
public interface PublishedHostRepository extends JpaRepository<PublishedHost,String> {
    @Query(value = "select * from published_host where update_time >= timestamp order by host_name asc", nativeQuery = true)
    List<PublishedHost> getAllBy(Time timestamp);

    @Query(value="select * from published_host where host_name=:host_name",nativeQuery = true)
    PublishedHost getByHostName(@Param("host_name")String host_name);

    @Query(value="select * from published_host where id=:id",nativeQuery = true)
    PublishedHost getById(@Param("id")String id);

    @Modifying
    @Query(value="update published_host ph set ph.host_name=newName where ph.host_name=oldName",nativeQuery = true)
    void updateHostName(String newName,String oldName);

    @Modifying
    @Query(value="update published_host ph set ph.id=?1 where ph.host_name=?2",nativeQuery = true)
    void updateHostId(String id, String hostName);

    @Modifying
    @Query(value="udpate published_host ph set ph.channels=?2 where ph.id=?1",nativeQuery = true)
    void updateChannels(String id, String channels);
}
