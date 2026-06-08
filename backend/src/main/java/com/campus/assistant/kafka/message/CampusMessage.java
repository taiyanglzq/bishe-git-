package com.campus.assistant.kafka.message;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ???? ???????? Kafka ???????????
 */
@Data
@Builder
public class CampusMessage {

    private String messageId;
    private String bizType;
    private Long bizId;
    private Long operatorId;
    private LocalDateTime createTime;
    private Map<String, Object> payload;
    private String failReason;
}
