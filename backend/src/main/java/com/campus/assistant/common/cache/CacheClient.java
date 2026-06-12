package com.campus.assistant.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类，负责统一封装项目中的缓存读写、空值缓存和缓存删除逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, Class<T> type) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            log.info("CACHE_MISS key={}", key);  // 缓存未命中
            return null;
        }
        if (CacheKeyConstants.CACHE_NULL_VALUE.equals(json)) {
            log.info("CACHE_HIT_NULL key={}", key);  // 命中空值缓存
            return null;
        }
        try {
            T result = objectMapper.readValue(json, type);   // JSON 反序列化
            log.info("CACHE_HIT key={}", key);  // 缓存命中
            return result;
        } catch (JsonProcessingException e) {
            log.warn("CACHE_DESERIALIZE_FAIL key={}", key, e);   // 反序列化失败
            delete(key); // 删除损坏的缓存
            return null;
        }
    }

    public String getRaw(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            log.info("CACHE_MISS key={}", key);
            return null;
        }
        if (CacheKeyConstants.CACHE_NULL_VALUE.equals(value)) {
            log.info("CACHE_HIT_NULL key={}", key);
            return null;
        }
        log.info("CACHE_HIT key={}", key);
        return value;
    }

    public boolean hasKey(String key) {
        Boolean result = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    public void set(String key, Object value, long ttl, TimeUnit unit) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl, unit);
            log.info("CACHE_SET key={} ttl={} {}", key, ttl, unit);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("缓存序列化失败，key=" + key, e);
        }
    }

    public void setRaw(String key, String value, long ttl, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, ttl, unit);
        log.info("CACHE_SET key={} ttl={} {}", key, ttl, unit);
    }

    public void setNull(String key, long ttl, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, CacheKeyConstants.CACHE_NULL_VALUE, ttl, unit);
        log.info("CACHE_SET_NULL key={} ttl={} {}", key, ttl, unit);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
        log.info("CACHE_DELETE key={}", key);
    }

    public void delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        stringRedisTemplate.delete(keys);
        log.info("CACHE_DELETE_BATCH size={} keys={}", keys.size(), keys);
    }

    public void deleteKeys(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        delete(java.util.Arrays.asList(keys));
    }

    public Collection<String> scan(String pattern) {
        Collection<String> keys = stringRedisTemplate.keys(pattern);
        return keys == null ? Collections.emptyList() : keys;
    }
}
