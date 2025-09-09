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

    private String buildKey(String lectureId, Long memberId){
        return KEY_PREFIX + lectureId + ":" + memberId;
    }

    public void saveChatMessage(String lectureId, Long memberId, String message) {
        String key = buildKey(lectureId, memberId);
        log.info("key={} message={}", key, message);

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.opsForList().trim(key, -MAX_MESSAGE,-1);
        redisTemplate.expire(key, TIME_TO_LIVE);  // 2, TimeUnit.HOURS
    }

    public List<String> getChatMessage(String lectureId, Long memberId) {
        String key = buildKey(lectureId, memberId);
        List<String> chatHistory = redisTemplate.opsForList().range(key, 0, -1);
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
