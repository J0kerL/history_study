package com.history.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ModelScope 图像生成配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "modelscope")
public class ModelScopeImageProperties {

    /** API Key */
    private String apiKey;

    /** API 基础地址 */
    private String baseUrl = "https://api-inference.modelscope.cn";

    /** 模型名称 */
    private String model = "Tongyi-MAI/Z-Image-Turbo";

    /** 图片宽度（px） */
    private int width = 720;

    /** 图片高度（px） */
    private int height = 400;

    /** 推理步数 */
    private int numInferenceSteps = 9;

    /** 引导系数 */
    private double guidanceScale = 0.0;

    /** 轮询间隔（毫秒） */
    private int pollingIntervalMs = 2000;

    /** 最大轮询次数 */
    private int maxPollingAttempts = 60;
}
