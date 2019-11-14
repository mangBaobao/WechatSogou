package cert.aiops.pega.util;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PropertySource("classpath:utility.properties")
public class KafkaUtil {
    private Logger logger = LoggerFactory.getLogger(KafkaUtil.class);
    @Value("${pega.kafka.producer.topics}")
    private String topicsAsString;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public boolean send2Kafka(String topic,Object object){
        if(!topicsAsString.contains(topic)||topic.isEmpty()){
            logger.info("send2Kafka: input topic is not included,refused to send. topic={}",topic);
            return  false;
        }
        kafkaTemplate.send(topic,object);
        return  true;
    }

    @KafkaListener(topics="#{pega.kafka.consumer.topics.exception")
    public void processException(List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer){

    }
}
