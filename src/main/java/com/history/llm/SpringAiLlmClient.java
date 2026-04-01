package com.history.llm;

import com.history.exception.BusinessException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring AI ChatClient 的统一大模型客户端实现。
 */
@Component
public class SpringAiLlmClient implements LlmClient {

    private final ChatClient chatClient;

    public SpringAiLlmClient(ChatClient historyChatClient) {
        this.chatClient = historyChatClient;
    }

    @Override
    public String call(String userPrompt) {
        try {
            return chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new BusinessException("大模型调用失败", e);
        }
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new BusinessException("大模型调用失败", e);
        }
    }

    @Override
    public <T> T call(String systemPrompt, String userPrompt, Class<T> responseType) {
        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(responseType);
        } catch (Exception e) {
            throw new BusinessException("大模型结构化输出失败", e);
        }
    }
}
