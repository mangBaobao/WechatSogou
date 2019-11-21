package cert.aiops.pega.registration;


import cert.aiops.pega.bean.AgentException;
import cert.aiops.pega.bean.Judgement;
import cert.aiops.pega.dao.JudgementDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationExceptionListener implements BatchAcknowledgingConsumerAwareMessageListener<String, String> {
    private Logger logger = LoggerFactory.getLogger(RegistrationExceptionListener.class);

    private final int pack=100;
    @Autowired
   private RegistrationManager registrationManager;
    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        ObjectMapper mapper=new ObjectMapper();
        ArrayList<AgentException> exceptions=new ArrayList<>();
        AgentException exception;
        for(ConsumerRecord<String,String> singleRecord:data){
            logger.info("onMessage receive arrival record: value={}",singleRecord.value());
            try {
                 exception = mapper.readValue(singleRecord.value(),AgentException.class);
                 exceptions.add(exception);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(exceptions.size()==pack) {
                logger.info("onMessage: call registrationManager addAgentExceptionList count={}",exceptions.size());
                registrationManager.addAgentExceptionList(exceptions);
                exceptions.clear();
            }
        }
        if(exceptions.size()!=0){
            logger.info("onMessage: call registrationManager addAgentExceptionList count={}",exceptions.size());
            registrationManager.addAgentExceptionList(exceptions);
        }
    }
}
