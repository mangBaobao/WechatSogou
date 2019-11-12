package cert.aiops.pega.dao;

import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.bean.SystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemInfoRepository  extends JpaRepository<SystemInfo, Long> {
    @Query(value = "select distinct system_name from system_info order by id", nativeQuery = true)
    List<String> findDistinctBySystemName();

    @Query(value="select * from system_info where system_name=?1",nativeQuery=true)
    SystemInfo  findBySystemName(String systemName);

    @Modifying
    @Query(value = "delete from system_info  where system_name=?1",nativeQuery = true)
    int deleteBySystemName(String systemName);

    @Query(value="select * from system_info where id=?1",nativeQuery = true)
    SystemInfo findBySystemId(long systemId);

//    @Query(value="select * from system_info",nativeQuery = true)
//    List<SystemInfo> getAllSystemInfos();
}


