package cert.aiops.pega.exceptions;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.BatchConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

public class KafkaMessageListener implements BatchAcknowledgingConsumerAwareMessageListener<String, String> {
    @Override
    public void onMessage(List<ConsumerRecord<String, String>> list, Consumer<?, ?> consumer) {

    }

    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {

    }
}
