package cert.aiops.pega.registration;

import cert.aiops.pega.bean.AgentException;
import cert.aiops.pega.util.PegaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RegistrationManager {
    private Logger logger= LoggerFactory.getLogger(RegistrationManager.class);
    private ConcurrentLinkedQueue<AgentException> arrivalExceptions;
    private HashMap<String, PegaEnum.IssueStatus> exceptionStatus;


    public RegistrationManager(){
        arrivalExceptions=new ConcurrentLinkedQueue<>();
    }


    public void addAgentException(AgentException a){
        arrivalExceptions.add(a);
    }

    public void addAgentExceptionList(ArrayList<AgentException> agentExceptionList){
        arrivalExceptions.addAll(agentExceptionList);
    }
}
