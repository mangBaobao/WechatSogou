package cert.aiops.pega.service;

import cert.aiops.pega.bean.RegistrationException;
import cert.aiops.pega.dao.RegistrationExceptionRepository;
import cert.aiops.pega.util.PegaEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class RegistrationExceptionService {
    @Autowired
    private RegistrationExceptionRepository registrationExceptionRepository;

    public List<RegistrationException>  getExceptionsByCode(PegaEnum.RegistrationExceptionCode code){
        return registrationExceptionRepository.getAllByCode(code.name());
    }

    public void storeException(RegistrationException re){
        registrationExceptionRepository.save(re);
    }
}
