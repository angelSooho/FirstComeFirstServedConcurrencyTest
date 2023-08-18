package com.example.firstcomefirstservedconcurrencytest.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AppliedUserRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Long add(Long userId) {
        return redisTemplate
                .opsForSet()
                .add("applied_user", String.valueOf(userId));
    }
}
