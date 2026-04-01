package com.history.service.impl;

import com.history.llm.LlmClient;
import com.history.service.LlmTestService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 大模型连通性测试服务实现。
 */
@Service
public class LlmTestServiceImpl implements LlmTestService {

    @Resource
    private LlmClient llmClient;

    @Override
    public String testConnection() {
        return llmClient.call(
                "你是一个历史学习应用的AI助手。请只返回一句简短中文，不要使用 Markdown，不要补充额外说明。",
                "请回复：大模型连接成功。"
        );
    }
}
