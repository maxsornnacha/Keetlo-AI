package com.keetlo.ai.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionService {
    private final RedisTemplate<String, String> redisTemplate;

    public SessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeToken(String token, String userId, long expireSeconds) {
        redisTemplate.opsForValue().set(token, userId, Duration.ofSeconds(expireSeconds));
    }

    public String getUserIdByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void removeToken(String token) {
        redisTemplate.delete(token);
    }
}
