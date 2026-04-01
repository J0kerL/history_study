package com.history.controller;

import com.history.common.Result;
import com.history.service.LlmTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大模型测试接口。
 */
@RestController
@RequestMapping("/llm")
@Tag(name = "大模型测试", description = "用于验证大模型是否已正确接通")
public class LlmTestController {

    @Resource
    private LlmTestService llmTestService;

    @GetMapping("/test")
    @Operation(summary = "测试大模型连通性", description = "调用大模型并返回一段简短文本，用于验证配置是否生效")
    public Result<String> testConnection() {
        return Result.success(llmTestService.testConnection());
    }
}
