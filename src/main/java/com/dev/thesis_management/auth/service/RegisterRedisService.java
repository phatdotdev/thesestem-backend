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
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class RegisterRedisService {
    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper;

    public void save(RegisterInfo info) {
        try {
            String key = "register:" + info.getEmail();
            String json = objectMapper.writeValueAsString(info);

            long REGISTER_TTL = 10;
            redisTemplate.opsForValue().set(
                    key,
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
                    .get("register:" + email);

            if (json == null) return null;

            return objectMapper.readValue(json, RegisterInfo.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read register info", e);
        }
    }

    public void delete(String email) {
        redisTemplate.delete("register:" + email);
    }
}
