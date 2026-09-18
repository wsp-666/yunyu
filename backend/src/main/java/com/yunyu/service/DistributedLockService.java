package com.yunyu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 基于 Redis SET NX 的分布式锁，保证多实例下单/抽位/兑换互斥。
 */
@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public <T> T executeWithLock(String lockKey, Duration leaseTime, long waitMillis, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        long deadline = System.currentTimeMillis() + waitMillis;
        boolean locked = false;
        try {
            while (System.currentTimeMillis() < deadline) {
                Boolean ok = stringRedisTemplate.opsForValue()
                        .setIfAbsent(lockKey, token, leaseTime);
                if (Boolean.TRUE.equals(ok)) {
                    locked = true;
                    break;
                }
                Thread.sleep(30);
            }
            if (!locked) {
                throw new IllegalStateException("系统繁忙，请稍后重试");
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取锁被中断");
        } finally {
            if (locked) {
                unlock(lockKey, token);
            }
        }
    }

    public boolean tryLock(String lockKey, Duration leaseTime) {
        String token = UUID.randomUUID().toString();
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, token, leaseTime);
        if (Boolean.TRUE.equals(ok)) {
            // 定时任务场景：租约内自然过期即可，不在此持有 token
            return true;
        }
        return false;
    }

    public void unlock(String lockKey, String token) {
        try {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), token);
        } catch (Exception e) {
            log.warn("unlock failed key={}: {}", lockKey, e.getMessage());
        }
    }
}
