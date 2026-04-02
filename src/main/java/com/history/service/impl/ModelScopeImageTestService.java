package com.history.service.impl;

import com.history.llm.ModelScopeImageClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ModelScope 文生图大模型连通性测试服务实现。
 */
@Slf4j
@Service
public class ModelScopeImageTestService {

    private static final String IMAGE_DIR = "image";

    @Resource
    private ModelScopeImageClient modelScopeImageClient;

    /**
     * 测试 ModelScope 文生图模型连通性。
     * 生成一张简单的测试图片，返回本地文件路径和 OSS URL。
     *
     * @return 测试结果信息
     */
    public String testConnection() {
        String prompt = "一只可爱的小蜗牛坐在键盘上，卡通风格，色彩明亮温馨";
        Path localDir = Paths.get(IMAGE_DIR);

        Path imagePath;
        try {
            imagePath = modelScopeImageClient.generateAndDownload(prompt, localDir);
        } catch (Exception e) {
            log.error("文生图模型连通性测试失败: {}", e.getMessage());
            return "文生图模型连接测试失败: " + e.getMessage();
        }

        log.info("文生图模型连通性测试通过，图片已保存至: {}", imagePath);
        return "文生图模型连接测试成功！图片已保存至: " + imagePath.toAbsolutePath();
    }
}
