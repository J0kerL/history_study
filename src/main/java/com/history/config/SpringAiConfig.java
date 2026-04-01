package com.history.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 基础配置。
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient historyChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
