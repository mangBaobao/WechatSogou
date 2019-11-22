package cert.aiops.pega.registration;

import cert.aiops.pega.util.KafkaUtil;
import org.apache.logging.log4j.spi.LoggerRegistry;
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

    @Scheduled(cron="0 0/2 * * * *")
    public void startReceiveExceptions(){
        logger.info("startReceiveExceptions: begins to receive agent exceptions");
        RegistrationExceptionListener listener=new RegistrationExceptionListener();
        kafkaUtil.startConsumeMessage("exception",listener);
    }

    @Scheduled(cron="0 0/3 * * * *")
    public void stopReceiveException(){
        logger.info("stopReceiveException: begins to stop receive agent exceptions");
        kafkaUtil.pauseConsumeMessage();
    }
}
