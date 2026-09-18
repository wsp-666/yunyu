package com.yunyu.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 统一 Redis 缓存读写（JSON），多实例共享。
 */
@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public <T> T getOrLoad(String key, Duration ttl, Class<T> clazz, Supplier<T> loader) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                return objectMapper.readValue(cached, clazz);
            }
        } catch (Exception e) {
            log.warn("Redis get miss/error key={}: {}", key, e.getMessage());
        }

        T value = loader.get();
        if (value != null) {
            put(key, value, ttl);
        }
        return value;
    }

    public <T> T getOrLoad(String key, Duration ttl, TypeReference<T> typeRef, Supplier<T> loader) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                return objectMapper.readValue(cached, typeRef);
            }
        } catch (Exception e) {
            log.warn("Redis get miss/error key={}: {}", key, e.getMessage());
        }

        T value = loader.get();
        if (value != null) {
            put(key, value, ttl);
        }
        return value;
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.warn("Redis put error key={}: {}", key, e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete error key={}: {}", key, e.getMessage());
        }
    }

    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis deleteByPattern error pattern={}: {}", pattern, e.getMessage());
        }
    }
}
