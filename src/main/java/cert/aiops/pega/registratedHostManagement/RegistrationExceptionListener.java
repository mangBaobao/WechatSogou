package cert.aiops.pega.registratedHostManagement;


import cert.aiops.pega.bean.RegistrationException;
import cert.aiops.pega.util.SpringContextUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationExceptionListener implements BatchAcknowledgingConsumerAwareMessageListener<String, String> {
    private Logger logger = LoggerFactory.getLogger(RegistrationExceptionListener.class);

    private final int pack=100;

    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        ObjectMapper mapper=new ObjectMapper();
        ArrayList<RegistrationException> exceptions=new ArrayList<>();
        RegistrationException exception;
        RegistratedHostManager registratedHostManager = SpringContextUtil.getBean(RegistratedHostManager.class);
        for(ConsumerRecord<String,String> singleRecord:data){
            logger.info("onMessage receive arrival record: value={}",singleRecord.value());
            try {
                 exception = mapper.readValue(singleRecord.value(), RegistrationException.class);
                 exceptions.add(exception);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(exceptions.size()==pack) {
                logger.info("onMessage: call registrationManager addRegistrationExceptionList count={}",exceptions.size());
                registratedHostManager.addRegistrationExceptionList(exceptions);
                exceptions.clear();
            }
        }
        if(exceptions.size()!=0){
            logger.info("onMessage: call registrationManager addRegistrationExceptionList count={}",exceptions.size());
            registratedHostManager.addRegistrationExceptionList(exceptions);
        }
    }
}
