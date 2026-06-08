package com.campus.assistant.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ???? ?????????????? Kafka ???
 */
@Slf4j
@Component
public class CampusMessageConsumer {

    @KafkaListener(topics = {"ca-notice-topic", "ca-booking-topic", "ca-checkin-topic", "ca-log-topic"}, groupId = "campus_assistant_group")
    public void consume(String message) {
        log.info("接收到 Kafka 消息：{}", message);
    }
}
