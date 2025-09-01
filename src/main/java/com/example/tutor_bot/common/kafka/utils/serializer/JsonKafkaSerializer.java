package com.example.tutor_bot.common.kafka.utils.serializer;

import com.example.tutor_bot.common.kafka.utils.KafkaObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class JsonKafkaSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = KafkaObjectMapper.create();

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Kafka JSON Serialize error", e);
        }
    }
}