package com.yunyu.service;

import com.yunyu.common.CacheKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 简易滑动窗口限流（Redis），保护热点写接口。
 */
@Service
public class RateLimitService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @return true 表示允许通过
     */
    public boolean tryAcquire(String api, String identity, int maxRequests, Duration window) {
        String key = CacheKeys.rateLimit(api, identity);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, window);
        }
        return count != null && count <= maxRequests;
    }
}
