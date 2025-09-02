package com.example.tutor_bot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

    @Value("classpath:prompts/system-prompt.st")
    private Resource systemPromptResource;

    @Value("${spring.ai.bedrock.converse.chat.options.max-tokens}")
    private int maxTokens;

    @Value("${spring.ai.bedrock.converse.chat.options.model}")
    private String model;

    @Value("${spring.ai.bedrock.converse.chat.options.temperature}")
    private Double temperature;

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        ChatOptions defaultOptions = ChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        return ChatClient.builder(chatModel)
                .defaultOptions(defaultOptions)
                .defaultSystem(systemPromptResource)
                .build();

    }
}
