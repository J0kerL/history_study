package com.history.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.history.exception.BusinessException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SpringAiLlmClient implements LlmClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public SpringAiLlmClient(ChatClient historyChatClient, ObjectMapper objectMapper) {
        this.chatClient = historyChatClient;
        this.objectMapper = objectMapper;
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
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            return objectMapper.readValue(normalizeJson(content), responseType);
        } catch (Exception e) {
            throw new BusinessException("大模型结构化输出失败", e);
        }
    }

    private String normalizeJson(String content) throws JsonProcessingException {
        if (content == null) {
            throw new JsonProcessingException("empty content") {
            };
        }

        String json = content.trim();
        if (json.startsWith("```")) {
            json = stripMarkdownFence(json);
        }
        json = extractJsonObject(json);
        return escapeInnerQuotesInJson(json);
    }

    private String stripMarkdownFence(String content) {
        String stripped = content.replaceFirst("^```(?:json)?\\s*", "");
        return stripped.replaceFirst("\\s*```$", "").trim();
    }

    private String extractJsonObject(String content) throws JsonProcessingException {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new JsonProcessingException("no json object found") {
            };
        }
        return content.substring(start, end + 1);
    }

    private String escapeInnerQuotesInJson(String json) {
        StringBuilder builder = new StringBuilder(json.length() + 32);
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);

            if (current == '"' && !escaped) {
                if (!inString) {
                    inString = true;
                    builder.append(current);
                    continue;
                }

                char nextSignificant = findNextSignificantChar(json, i + 1);
                if (nextSignificant == ',' || nextSignificant == '}' || nextSignificant == ']' || nextSignificant == ':') {
                    inString = false;
                    builder.append(current);
                    continue;
                }

                builder.append("\\\"");
                continue;
            }

            builder.append(current);
            if (inString && current == '\\' && !escaped) {
                escaped = true;
            } else {
                escaped = false;
            }
        }

        return builder.toString();
    }

    private char findNextSignificantChar(String json, int fromIndex) {
        for (int i = fromIndex; i < json.length(); i++) {
            char current = json.charAt(i);
            if (!Character.isWhitespace(current)) {
                return current;
            }
        }
        return '\0';
    }
}
