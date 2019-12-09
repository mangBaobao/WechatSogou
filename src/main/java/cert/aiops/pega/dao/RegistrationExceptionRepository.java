package cert.aiops.pega.dao;

import cert.aiops.pega.bean.RegistrationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegistrationExceptionRepository extends JpaRepository<RegistrationException, String> {

    @Query(value="select * from registration_exception where code=?1 order by time desc", nativeQuery = true)
    public List<RegistrationException> getAllByCode(String exceptionCode);

}
