package cert.aiops.pega.config;


import cert.aiops.pega.startup.BeingMasterCondition;
import cert.aiops.pega.startup.BeingWorkerCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitAccessor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;


@Configuration
@PropertySource("classpath:utility.properties")
//@Conditional(value={BeingWorkerCondition.class})
public class MQConfigurer {
    Logger logger = LoggerFactory.getLogger(MQConfigurer.class);
    @Autowired
    private Environment environment;

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(environment.getProperty("spring.rabbitmq.addresses"));
        connectionFactory.setUsername(environment.getProperty("spring.rabbitmq.username"));
        connectionFactory.setPassword(environment.getProperty("spring.rabbitmq.password"));
        connectionFactory.setVirtualHost(environment.getProperty("spring.rabbitmq.virtual-host"));
        connectionFactory.setPublisherConfirms(environment.getProperty("spring.rabbitmq.publisher-confirms",Boolean.class));
        return  connectionFactory;
    }

  @Bean
    public RabbitTemplate rabbitTemplate(){
      RabbitTemplate template = new RabbitTemplate(connectionFactory());
      template.setMandatory(environment.getProperty("spring.rabbitmq.template.mandatory",boolean.class));
//      template.setExchange(environment.getProperty("spring.rabbitmq.template.createExchange"));
//      template.setConfirmCallback(((correlationData, b, s) -> {
//          if(b){
//              logger.info("send message to createExchange done, id:{}",correlationData.getHost_name());
//          }
//          else{
//              logger.error("send message to createExchange failed, cause:{}",s);
//          }
//      }));
      return template;
  }

  @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin admin= new RabbitAdmin(connectionFactory);
       return admin;
  }




//  @Bean
//  @Conditional(value={BeingWorkerCondition.class})
//  public Queue queue(){
//      Queue queue = new Queue(environment.getProperty("pega.worker.queue.name"),true,true,true);
//      return  queue;
//  }

@Bean
@Conditional(value={BeingWorkerCondition.class})
public RabbitListenerContainerFactory<SimpleMessageListenerContainer> manualAckContainerFactory(ConnectionFactory connectionFactory){
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory) ;
    factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    factory.setConcurrentConsumers(environment.getProperty("spring.rabbitmq.listener.concurrency",int.class));
    factory.setMaxConcurrentConsumers(environment.getProperty("spring.rabbitmq.listener.max-concurrency",int.class));
    factory.setPrefetchCount(environment.getProperty("spring.rabbit.listener.prefetch",int.class));
    logger.info("manualAckContainerFactory is created");
    return factory;
}
}
