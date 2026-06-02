package com.campus.assistant.kafka.producer;

import com.campus.assistant.kafka.message.CampusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampusMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, CampusMessage message) {
        kafkaTemplate.send(topic, message.getMessageId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 消息发送失败，topic={}, messageId={}", topic, message.getMessageId(), ex);
                        sendToDlq(topic + ".DLQ", message, ex.getMessage());
                    }
                });
    }

    public void sendToDlq(String dlqTopic, CampusMessage message, String failReason) {
        message.setFailReason(failReason);
        kafkaTemplate.send(dlqTopic, message.getMessageId(), message);
    }
}
