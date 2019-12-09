package cert.aiops.pega.dao;

import cert.aiops.pega.bean.HostInfo;
import cert.aiops.pega.util.PegaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HostInfoRepository extends JpaRepository<HostInfo, Long> {

    @Query(value = "select * from host_info where system_name=?1 order by id asc", nativeQuery = true)
    List<HostInfo> findBySystem_name(String systemName);

//    @Query(value="select * from host_info  where ip=:ip and net=:net",nativeQuery = true)
//    HostInfo getByIpNet(@Param("ip")String ip, @Param("net")String net);

    @Query(value="select * from host_info  where ip=:ip ",nativeQuery = true)
    HostInfo getByIp(@Param("ip")String ip);



    @Query(value = "select * from host_info where system_id=?1 and id >=?2 and id<= ?3 order by id asc", nativeQuery = true)
    List<HostInfo> findBySystem_IdIPRange(long systemId, long head, long tail);

//    @Modifying
//    @Query(value = "delete from host_info  where net=:net and ip=:ip",nativeQuery = true)
//    int deleteByIp(@Param("ip")String ip, @Param("net") String net);

    @Modifying
    @Query(value = "delete from host_info  where ip=:ip ",nativeQuery = true)
    int deleteByIp(@Param("ip") String ip);


    @Query(value = "select distinct system_name from host_info", nativeQuery = true)
    List<String> findDistinctBySystem_name();

    @Modifying
    @Query(value="update host_info hi set hi.system_name=?2 where hi.system_id=?1",nativeQuery = true)
    void updateSystemName(String systemId, String newName);
//    @Modifying
//    @Query(value = "insert into host_info(ip,host_name,net,state,system_name,create_time,update_time) " +
//            "value (?1,?2,?3,?4,?5,?6,?7)", nativeQuery = true)
//    int addHostInfo(String ip, String host_name, HostInfo.Net net,
//                    HostInfo.State state, String system_name, String create_time, String update_time);

    @Query(value="select * from host_info where state=?1 order by update_time desc",nativeQuery = true)
    List<HostInfo>  getAllByState(String state);

    @Query(value="select * from host_info where update_time>?1 order by update_time asc",nativeQuery = true)
    List<HostInfo> getAllByUpdateTime(String time);

}
