package cert.aiops.pega.exceptions;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

public class KafkaMessageListener implements BatchAcknowledgingConsumerAwareMessageListener<String, String> {
    private Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);
    @Override
    public void onMessage(List<ConsumerRecord<String, String>> list, Consumer<?, ?> consumer) {

        for(ConsumerRecord<String,String> singleRecord:list){
            logger.info("onMessage1: key={},value={}",singleRecord.key(),singleRecord.value());
        }
    }

    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        for(ConsumerRecord<String,String> singleRecord:data){
            logger.info("onMessage2: key={},value={}",singleRecord.key(),singleRecord.value());
        }
    }
}
