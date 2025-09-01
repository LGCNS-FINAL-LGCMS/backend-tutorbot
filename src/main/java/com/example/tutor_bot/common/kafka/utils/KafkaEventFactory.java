package com.example.tutor_bot.common.kafka.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.tutor_bot.common.kafka.dto.KafkaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventFactory {

    private final ObjectMapper objectMapper;

    public <T> T convert(KafkaEvent<?> event, Class<T> clazz) {
        return objectMapper.convertValue(event.getData(), clazz);
    }
}