package com.campus.assistant.kafka.dlq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CampusDlqConsumer {

    @KafkaListener(topics = {"ca-notice-topic.DLQ", "ca-booking-topic.DLQ", "ca-checkin-topic.DLQ", "ca-log-topic.DLQ"}, groupId = "campus_assistant_dlq_group")
    public void consumeDlq(String message) {
        log.warn("接收到 DLQ 死信消息，等待补偿或人工排查：{}", message);
    }
}
