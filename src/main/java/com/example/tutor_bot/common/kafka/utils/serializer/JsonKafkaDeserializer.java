package com.example.tutor_bot.common.kafka.utils.serializer;

import com.example.tutor_bot.common.kafka.utils.KafkaObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonKafkaDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = KafkaObjectMapper.create();
    private final Class<T> targetType;

    public JsonKafkaDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Kafka JSON Deserialize error", e);
        }
    }
}