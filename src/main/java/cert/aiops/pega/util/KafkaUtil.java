package cert.aiops.pega.util;

import cert.aiops.pega.config.KafkaConsumerConfiguer;
import cert.aiops.pega.registration.RegistrationExceptionListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:utility.properties")
public class KafkaUtil {
    private Logger logger = LoggerFactory.getLogger(KafkaUtil.class);
    @Value("${pega.kafka.producer.topics}")
    private String topicsAsString;

    private  ConcurrentMessageListenerContainer<String,String> container;
    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private KafkaConsumerConfiguer consumerConfiguer;

    public boolean send2Kafka(String topic,Object object){
        if(!topicsAsString.contains(topic)||topic.isEmpty()){
            logger.info("send2Kafka: input topic is not included,refused to send. topic={}",topic);
            return  false;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            kafkaTemplate.send(topic,mapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return  true;
    }

//    @KafkaListener(topics="#{pega.kafka.consumer.topics.exception")
//    public void processException(List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer){
//
//    }

    public void startConsumeMessage(String topic, RegistrationExceptionListener kafkaMessageListener){
        if(!topicsAsString.contains(topic)||topic.isEmpty()){
            logger.info("startConsumeMessage: input topic is not included,refused to send. topic={}",topic);
            return;
        }
        if(container==null) {
            container = consumerConfiguer.kafkaListenerContainerFactory().createContainer(topic);
            container.setupMessageListener(kafkaMessageListener);
        }
        if(!container.isRunning()) {
          logger.info("startConsumeMessage: container is not running. begin to start container");
            container.start();
        }
//        else{
//            logger.info("startConsumeMessage: container is  running. do nothing");
//            return;
//        }
        logger.info("startConsumeMessage:resume container");
        container.resume();
    }

    public void pauseConsumeMessage(){
        if(container==null){
            logger.info("stopConsumeMessage: container is not exist. do nothing...");
            return;
        }
        if(container.isRunning()||!container.isContainerPaused()){
            logger.info("stopConsumeMessage: begin to pause container");
            container.pause();
        }
        else{
            logger.info("stopConsumeMessage: container is paused or not running. Do nothing...");
        }
    }
}
