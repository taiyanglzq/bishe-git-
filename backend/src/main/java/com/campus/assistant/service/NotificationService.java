package com.campus.assistant.service;

public interface NotificationService {

    void send(Long receiverId, String title, String content, String bizType, Long bizId);
}
