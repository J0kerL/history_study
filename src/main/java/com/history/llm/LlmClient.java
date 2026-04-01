package com.history.llm;

/**
 * 统一的大模型客户端接口。
 */
public interface LlmClient {

    String call(String userPrompt);

    String call(String systemPrompt, String userPrompt);

    <T> T call(String systemPrompt, String userPrompt, Class<T> responseType);
}
