package com.dev.thesis_management.auth.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class RefreshTokenRedisService {
    StringRedisTemplate redisTemplate;

    public void save(UUID userId, String refreshToken){
        redisTemplate.opsForValue().set("refresh_token:" + userId,
                refreshToken,
                7,
                TimeUnit.DAYS);
    }

    public String get(UUID userId) {
        return redisTemplate.opsForValue()
                .get("refresh_token:" + userId);
    }

    public void delete(UUID userId) {
        redisTemplate.delete("refresh_token:" + userId);
    }
}
