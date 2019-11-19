package cert.aiops.pega.config;

import cert.aiops.pega.util.KafkaUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@PropertySource("classpath:utility.properties")
public class KafkaConsumerConfiguer {

    private Logger logger = LoggerFactory.getLogger(KafkaConsumerConfiguer.class);

    @Autowired
    private Environment environment;

    public KafkaConsumerConfiguer(){
        logger.info("begins to load kafka consumer configurations ...");
    }

    @Bean
    public Map<String,Object> consumerProperties(){
        Map<String,Object> properties=new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,environment.getProperty("spring.kafka.bootstrap-servers"));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,environment.getProperty("spring.kafka.consumer.group-id"));
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,environment.getProperty("spring.kafka.consumer.enable-auto-commit"));
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,environment.getProperty("spring.kafka.consumer.key-deserializer"));
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,environment.getProperty("spring.kafka.consumer.value-deserializer"));
        return properties;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(consumerProperties());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> factory=new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(Integer.valueOf(environment.getProperty("spring.kafka.listener.concurrency")));
        factory.setBatchListener(true);
        factory.getContainerProperties().setPollTimeout(30000);
        return factory;
    }


}
