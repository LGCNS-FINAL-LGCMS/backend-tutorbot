package com.example.tutor_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.ai.chat.memory.redis.options.capacity}")
    private int MAX_MESSAGE;

    @Value("${spring.ai.chat.memory.redis.key-prefix}")
    private String KEY_PREFIX;

    @Value("${spring.ai.chat.memory.redis.time-to-live}")
    private Duration TIME_TO_LIVE;

    private String buildKey(String lectureId, String userId){
        return KEY_PREFIX + lectureId + ":" + userId;
    }

    public void saveChatMessage(String lectureId, String userId, String message) {
        String key = buildKey(lectureId, userId);
        log.info("key={} message={}", key, message);

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.opsForList().trim(key, -MAX_MESSAGE,-1);
        redisTemplate.expire(key, TIME_TO_LIVE);  // 2, TimeUnit.HOURS
    }

    public List<String> getChatMessage(String lectureId, String userId) {
        String key = buildKey(lectureId, userId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
