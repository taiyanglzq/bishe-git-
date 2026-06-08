package com.campus.assistant.service.impl;

import com.campus.assistant.service.DelayTaskService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * ???? ????????????????????
 */
@Service
@RequiredArgsConstructor
public class DelayTaskServiceImpl implements DelayTaskService {

    private final RedissonClient redissonClient;

    @Override
    public void addTask(String queueName, String taskBody, Duration delay) {
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(queueName);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(taskBody, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}
