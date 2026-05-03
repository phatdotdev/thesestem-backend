package com.dev.thesis_management.auth.service;

import com.dev.thesis_management.auth.dto.RegisterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RegisterRedisService {

    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper;

    static final String PREFIX = "register:";
    static final long REGISTER_TTL = 10;

    private String key(String email) {
        return PREFIX + email;
    }

    public void save(RegisterInfo info) {
        try {

            String json = objectMapper.writeValueAsString(info);

            redisTemplate.opsForValue().set(
                    key(info.getEmail()),
                    json,
                    REGISTER_TTL,
                    TimeUnit.MINUTES
            );

        } catch (Exception e) {
            throw new IllegalStateException("Cannot save register info", e);
        }
    }

    public RegisterInfo get(String email) {
        try {

            String json = redisTemplate.opsForValue()
                    .get(key(email));

            if (json == null) return null;

            return objectMapper.readValue(json, RegisterInfo.class);

        } catch (Exception e) {
            throw new IllegalStateException("Cannot read register info", e);
        }
    }

    public boolean exists(String email) {

        Boolean exists = redisTemplate.hasKey(key(email));

        return Boolean.TRUE.equals(exists);
    }

    public void update(RegisterInfo info) {

        save(info);
    }

    public void updateVerified(String email) {

        RegisterInfo info = get(email);

        if (info == null) return;

        info.setVerified(true);

        save(info);
    }

    public long getTtl(String email) {

        Long ttl = redisTemplate.getExpire(
                key(email),
                TimeUnit.SECONDS
        );

        return ttl == null ? 0 : ttl;
    }

    public void delete(String email) {

        redisTemplate.delete(key(email));
    }
}