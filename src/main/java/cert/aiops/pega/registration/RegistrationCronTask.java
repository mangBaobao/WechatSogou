package cert.aiops.pega.registration;

import cert.aiops.pega.util.KafkaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RegistrationCronTask {

    private Logger logger= LoggerFactory.getLogger(RegistrationCronTask.class);
    @Autowired
    private KafkaUtil kafkaUtil;
    @Autowired
    private RegistrationManager registrationManager;

    @Scheduled(fixedDelay = 20000)
    public void startReceiveExceptions(){
        logger.info("startReceiveExceptions: begins to receive agent exceptions");
        RegistrationExceptionListener listener=new RegistrationExceptionListener();
        kafkaUtil.startConsumeMessage("exception",listener);
    }

    @Scheduled(fixedDelay =20000,initialDelay = 20000)
    public void stopReceiveException(){
        logger.info("stopReceiveException: begins to stop receive agent exceptions");
        kafkaUtil.pauseConsumeMessage();
    }

    @Scheduled(fixedDelay = 60000,initialDelay = 15000)
    public void incAdmitHosts(){
        logger.info("incAdmitHosts:begins to process claim messages and generate admit identification");
        registrationManager.publishAdmitIdentification();
        logger.info("incAdmitHosts:finishes to process claim messages and generate admit identification");
    }

    @Scheduled(fixedDelay = 60000,initialDelay = 45000)
    public void processExceptions(){
        logger.info("processExceptions:begins to process exception issues");
        registrationManager.processExceptionIssues();
        logger.info("processExceptions:finishes  to process exception issues");
    }
}
