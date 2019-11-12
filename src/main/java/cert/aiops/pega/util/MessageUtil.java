package cert.aiops.pega.util;

import cert.aiops.pega.bean.RequestTask;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@PropertySource("classpath:utility.properties")
//@Conditional(value={BeingWorkerCondition.class})
public class MessageUtil implements RabbitTemplate.ConfirmCallback ,RabbitTemplate.ReturnCallback{
    private static MessageUtil messageUtil;
    private Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    Environment environment;

    @Value("${spring.rabbitmq.exchange.name}")
    private String exchangeName;

    private DirectExchange directExchange;

    @PostConstruct
    public void init(){
        messageUtil = this;
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
        createExchange();
    }


    public static MessageUtil getInstance(){
        return messageUtil;
    }

    @Bean
    public void createExchange(){
        directExchange = new DirectExchange(exchangeName,true,false,null);
        rabbitAdmin.declareExchange(directExchange);
    }

    public void createQueue(String queueName){
        rabbitAdmin.declareQueue(new Queue(queueName,true));
    }

    public void createBinding(String queueName, String routingKey){
        Binding binding =new Binding(queueName,Binding.DestinationType.QUEUE,exchangeName,routingKey,new HashMap<>());
        rabbitAdmin.declareBinding(binding);
    }


    public boolean deleteQueue(String queueName){
        return rabbitAdmin.deleteQueue(queueName);
    }


    public void removeBinding(String queueName,String routineKey){
        rabbitAdmin.removeBinding(BindingBuilder.bind(new Queue(queueName)).to(new TopicExchange(exchangeName)).with(routineKey));
    }

    public boolean ifQueueExist(String queueName){
        if(rabbitAdmin.getQueueProperties(queueName)!=null)
            return true;
        else
            return false;
    }

    public void sendMessage(String routingKey, RequestTask task, CorrelationData correlationData){
        Map<String,Object> properties =new HashMap<>();
        properties.put("send_time",System.currentTimeMillis());
    //    properties.put("test","helen123456");
        MessageHeaders messageHeaders = new MessageHeaders(properties);
        Message msg = MessageBuilder.createMessage(task,messageHeaders);
        logger.info("MessageUtil_sendMessage:{}",msg);
       rabbitTemplate.convertAndSend(environment.getProperty("spring.rabbitmq.exchange.name"),routingKey,msg,correlationData);
     //   Object response=rabbitTemplate.convertSendAndReceive(environment.getProperty("spring.rabbitmq.exchange.name"),routingKey,msg,correlationData);
   //     logger.info("MessageUtil_sendMessage:correlationData={},response={}",correlationData,response);
    }


    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    if(ack) {
        logger.info("confirm_CorrelationData:{}", correlationData);
        logger.info("confirm_ack:{}", ack);
    }else
            logger.info("confirm_fail,cause:{}",cause);
    }

    @Override
    public void returnedMessage(org.springframework.amqp.core.Message message, int replyCode, String replyText, String exchange, String routingKey) {
        logger.info("returndMessage:message:{},replyCode:{},replyText:{},exchange:{},routingKey:{}",
                new String(message.getBody(), StandardCharsets.UTF_8),replyCode,replyText,exchange,routingKey);
        logger.info("please try to send message Again....");
    }
}
