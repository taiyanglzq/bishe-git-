package com.campus.assistant.common.utils;

import com.campus.assistant.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RepeatSubmitLimiter {

    private final RedissonClient redissonClient;

    public RLock tryLock(String key, Duration waitTime, Duration leaseTime) {
        RLock lock = redissonClient.getLock("ca:repeat:" + key);
        try {
            boolean locked = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new BusinessException(409, "请求正在处理中，请勿重复提交");
            }
            return lock;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "重复提交限制处理失败");
        }
    }
}
