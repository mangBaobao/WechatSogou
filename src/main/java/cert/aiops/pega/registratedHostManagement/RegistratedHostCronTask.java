package cert.aiops.pega.registratedHostManagement;

import cert.aiops.pega.util.KafkaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RegistratedHostCronTask {

    private Logger logger= LoggerFactory.getLogger(RegistratedHostCronTask.class);
    @Autowired
    private KafkaUtil kafkaUtil;
    @Autowired
    private RegisteredHostManager registeredHostManager;

    @Scheduled(fixedDelay = 300000,initialDelay =60000)
    public void startReceiveExceptions(){
        logger.info("startReceiveExceptions: begins to receive agent exceptions");
        RegistrationExceptionListener listener=new RegistrationExceptionListener();
        kafkaUtil.startConsumeMessage("exception",listener);
    }

    @Scheduled(fixedDelay =300000,initialDelay = 80000)
    public void stopReceiveException(){
        logger.info("stopReceiveException: begins to stop receive agent exceptions");
        kafkaUtil.pauseConsumeMessage();
    }

    @Scheduled(fixedDelay = 300000,initialDelay = 100000)
    public void incAdmitHosts(){
        logger.info("incAdmitHosts:begins to process claim messages and generate admit identification");
        registeredHostManager.publishAdmitIdentification();
        logger.info("incAdmitHosts:finishes to process claim messages and generate admit identification");
    }

    @Scheduled(fixedDelay = 200000,initialDelay = 300000)
    public void publishUpdatedHosts(){
        logger.info("publishUpdatedHosts:begins to publish recent-updated host identification");
        registeredHostManager.publishUpdatedHosts();
        logger.info("publishUpdatedHosts:finishes to publish recent-updated host identification");
    }

    @Scheduled(fixedDelay = 300000,initialDelay = 100000)
    public void processExceptions(){
        logger.info("processExceptions:begins to process exception issues");
        registeredHostManager.processExceptionIssues();
        logger.info("processExceptions:finishes  to process exception issues");
    }
}
