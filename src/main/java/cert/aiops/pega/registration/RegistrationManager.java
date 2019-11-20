package cert.aiops.pega.registration;

import cert.aiops.pega.bean.AgentException;
import cert.aiops.pega.util.PegaEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RegistrationManager {
    private ConcurrentLinkedQueue<AgentException> arrivalExceptions;
    private HashMap<String, PegaEnum.IssueStatus> exceptionStatus;


    public RegistrationManager(){
        arrivalExceptions=new ConcurrentLinkedQueue<>();
    }


}
