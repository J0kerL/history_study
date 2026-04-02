package com.history.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.history.config.ModelScopeImageProperties;
import com.history.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * ModelScope 文生图 API 客户端。
 * 异步提交 → 轮询 → 下载到本地。
 */
@Slf4j
@Component
public class ModelScopeImageClient {

    @Resource
    private ModelScopeImageProperties properties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成图片并下载到本地目录。
     *
     * @param prompt 图片描述
     * @param saveDir 本地保存目录
     * @return 本地文件路径
     */
    public Path generateAndDownload(String prompt, Path saveDir) {
        log.info("提交图像生成任务: prompt={}", prompt);

        String taskId = submitTask(prompt);
        log.info("图像任务提交成功: taskId={}", taskId);

        String imageUrl = pollTask(taskId);
        log.info("图像生成完成，临时URL: {}", imageUrl);

        return downloadImage(imageUrl, saveDir);
    }

    /**
     * 提交异步生成任务。
     */
    private String submitTask(String prompt) {
        String url = properties.getBaseUrl() + "/v1/images/generations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getApiKey());
        headers.set("X-ModelScope-Async-Mode", "true");

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("model", properties.getModel());
        body.put("prompt", prompt);
        body.put("width", properties.getWidth());
        body.put("height", properties.getHeight());
        body.put("num_inference_steps", properties.getNumInferenceSteps());
        body.put("guidance_scale", properties.getGuidanceScale());

        HttpEntity<String> request = new HttpEntity<>(toJson(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.ACCEPTED) {
                throw new BusinessException("提交图像生成任务失败: " + response.getStatusCode() + " " + response.getBody());
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("task_id")) {
                return root.get("task_id").asText();
            }
            throw new BusinessException("提交图像生成任务失败，未返回 task_id: " + response.getBody());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("提交图像生成任务失败: " + e.getMessage());
        }
    }

    /**
     * 轮询任务状态，直到完成。
     */
    private String pollTask(String taskId) {
        String url = properties.getBaseUrl() + "/v1/tasks/" + taskId;
        int attempts = 0;

        while (attempts < properties.getMaxPollingAttempts()) {
            attempts++;

            try {
                Thread.sleep(properties.getPollingIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("等待图像生成时被中断");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + properties.getApiKey());
            headers.set("X-ModelScope-Task-Type", "image_generation");

            HttpEntity<Void> request = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());

                // task_status: PROCESSING → SUCCEED / FAILED
                String status = root.path("task_status").asText();

                if ("SUCCEED".equals(status)) {
                    return extractImageUrl(root);
                } else if ("FAILED".equals(status)) {
                    String errorMsg = root.has("message") ? root.get("message").asText() : "未知错误";
                    throw new BusinessException("图像生成任务失败: " + errorMsg);
                }
                // PROCESSING / PENDING / RUNNING → 继续轮询
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("轮询图像任务状态失败: taskId={}, attempt={}", taskId, attempts, e);
            }
        }

        throw new BusinessException("图像生成任务超时，请稍后重试");
    }

    /**
     * 从任务结果中提取图片 URL。
     * 实际响应格式: {"task_status":"SUCCEED", "output_images":["https://..."]}
     */
    private String extractImageUrl(JsonNode root) {
        // 直接取根节点的 output_images
        if (root.has("output_images") && root.get("output_images").isArray()) {
            JsonNode images = root.get("output_images");
            if (images.size() > 0) {
                return resolveUrlField(images.get(0));
            }
        }

        // 兼容 output.images[0] 格式
        JsonNode output = root.get("output");
        if (output != null) {
            if (output.has("images") && output.get("images").isArray()) {
                JsonNode images = output.get("images");
                if (images.size() > 0) {
                    return resolveUrlField(images.get(0));
                }
            }
            if (output.has("output_images") && output.get("output_images").isArray()) {
                JsonNode images = output.get("output_images");
                if (images.size() > 0) {
                    return resolveUrlField(images.get(0));
                }
            }
        }

        throw new BusinessException("图像生成成功但无法解析返回结果: " + root);
    }

    private String resolveUrlField(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isObject()) {
            if (node.has("url")) return node.get("url").asText();
            if (node.has("path")) return node.get("path").asText();
        }
        return null;
    }

    /**
     * 从远程 URL 下载图片到本地目录。
     */
    private Path downloadImage(String imageUrl, Path saveDir) {
        try {
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }

            String filename = System.currentTimeMillis() + ".webp";
            Path targetPath = saveDir.resolve(filename);

            var connection = URI.create(imageUrl).toURL().openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            try (var stream = connection.getInputStream()) {
                Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("图片下载完成: {}", targetPath);
            return targetPath;
        } catch (IOException e) {
            throw new BusinessException("下载图片失败: " + e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException("JSON 序列化失败: " + e.getMessage());
        }
    }
}
