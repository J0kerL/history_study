package com.history.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Spring AI 及 HTTP 客户端基础配置。
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient historyChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    /**
     * 通用 RestTemplate Bean，供 ModelScopeImageClient 等组件共用。
     * 统一配置连接超时和读取超时，避免在各处硬编码。
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }
}
