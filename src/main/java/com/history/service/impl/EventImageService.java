package com.history.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.history.config.OssProperties;
import com.history.llm.ModelScopeImageClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 事件封面图片生成服务。
 * 调用 ModelScope 文生图 → 下载到本地 → 上传 OSS → 返回 OSS URL。
 */
@Slf4j
@Service
public class EventImageService {

    private static final String IMAGE_DIR = "image";
    private static final String OSS_IMAGE_DIR = "event&people";

    @Resource
    private ModelScopeImageClient modelScopeImageClient;

    @Resource
    private OSS ossClient;

    @Resource
    private OssProperties ossProperties;

    public String generateEventImage(String imagePrompt) {
        Path localDir = Paths.get(IMAGE_DIR);

        // 1. 调用 ModelScope 生成并下载图片
        Path downloadedPath;
        try {
            downloadedPath = modelScopeImageClient.generateAndDownload(imagePrompt, localDir);
        } catch (Exception e) {
            log.error("生成事件封面图片失败: prompt={}", imagePrompt, e);
            return null;
        }

        // 2. 上传图片到 OSS
        String ossUrl = uploadToOss(downloadedPath);

        // 3. 清理本地临时文件
        try {
            Files.deleteIfExists(downloadedPath);
        } catch (IOException e) {
            log.warn("清理临时图片文件失败: {}", downloadedPath, e);
        }

        return ossUrl;
    }

    private String uploadToOss(Path filePath) {
        try {
            // 通过 OSS SDK 直接上传本地文件
            String ext = resolveExtension(filePath);
            String contentType = resolveContentType(ext);
            String objectKey = OSS_IMAGE_DIR + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

            // 复用已有的 OSS 上传逻辑（不依赖 MultipartFile）
            String urlPrefix = ossProperties.getUrlPrefix();
            if (urlPrefix.endsWith("/")) {
                urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 1);
            }

            // 通过注入的 OSS Client 上传
            var metadata = new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(Files.size(filePath));
            try (var is = Files.newInputStream(filePath)) {
                ossClient.putObject(ossProperties.getBucketName(), objectKey, is, metadata);
            }

            log.info("事件封面图片已上传到 OSS: objectKey={}", objectKey);
            return urlPrefix + "/" + objectKey;
        } catch (Exception e) {
            log.error("上传事件封面图片到 OSS 失败: file={}", filePath, e);
            return null;
        }
    }

    private String resolveExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(dot + 1) : "webp";
    }

    private String resolveContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case "webp" -> "image/webp";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "image/webp";
        };
    }
}
