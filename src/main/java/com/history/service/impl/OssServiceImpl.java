package com.history.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.history.config.OssProperties;
import com.history.exception.BusinessException;
import com.history.service.OssService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 阿里云 OSS 文件存储服务实现类。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Resource
    private OSS ossClient;

    @Resource
    private OssProperties ossProperties;

    /**
     * 将头像文件上传至阿里云 OSS，并返回可公网访问的 URL。
     * <p>
     * 上传流程：
     * 1. 校验文件类型，仅允许配置中声明的 MIME 类型。
     * 2. 校验文件大小，不超过配置的上限。
     * 3. 生成唯一 Object Key：{avatarDir}/{userId}/{UUID}.{ext}。
     * 4. 调用 OSS SDK 上传，上传失败时抛出 BusinessException。
     * 5. 拼接并返回文件访问 URL。
     *
     * @param userId 当前登录用户 ID
     * @param file   上传的图片文件
     * @return 头像文件的公网访问 URL
     */
    @Override
    public String uploadAvatar(long userId, MultipartFile file) {

        // 1. 文件非空校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 2. 文件类型校验：只允许配置中声明的图片 MIME 类型
        String contentType = file.getContentType();
        if (contentType == null || !ossProperties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException("文件类型不支持，仅允许上传 JPG、PNG、GIF、WEBP 格式图片");
        }

        // 3. 文件大小校验：不超过配置的上限（单位：MB）
        long maxBytes = ossProperties.getMaxFileSize() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("文件大小不能超过 " + ossProperties.getMaxFileSize() + " MB");
        }

        // 4. 生成唯一 Object Key：{avatarDir}/{userId}/{UUID}.{ext}
        String originalFilename = file.getOriginalFilename();
        String ext = extractExtension(originalFilename, contentType);
        String objectKey = ossProperties.getAvatarDir()
                + "/" + userId
                + "/" + UUID.randomUUID().toString().replace("-", "")
                + "." + ext;

        // 5. 上传文件到 OSS
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());
            ossClient.putObject(ossProperties.getBucketName(), objectKey, file.getInputStream(), metadata);
        } catch (OSSException e) {
            log.error("OSS 服务端异常，objectKey={}，errorCode={}，message={}",
                    objectKey, e.getErrorCode(), e.getMessage());
            throw new BusinessException("头像上传失败，请稍后重试");
        } catch (ClientException e) {
            log.error("OSS 客户端异常，objectKey={}，message={}", objectKey, e.getMessage());
            throw new BusinessException("头像上传失败，请检查网络后重试");
        } catch (IOException e) {
            log.error("读取上传文件流失败，objectKey={}，message={}", objectKey, e.getMessage());
            throw new BusinessException("文件读取失败，请重新上传");
        }

        // 6. 拼接并返回文件的公网访问 URL
        String urlPrefix = ossProperties.getUrlPrefix();
        // 确保 urlPrefix 末尾不含多余斜杠
        if (urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 1);
        }
        return urlPrefix + "/" + objectKey;
    }

    /**
     * 从原始文件名或 MIME 类型中提取文件扩展名。
     *
     * @param originalFilename 原始文件名（可为 null）
     * @param contentType      文件 MIME 类型
     * @return 文件扩展名（不含点号），默认返回 "jpg"
     */
    private String extractExtension(String originalFilename, String contentType) {
        // 优先从原始文件名中提取
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (!ext.isEmpty()) {
                return ext;
            }
        }
        // 兜底：根据 MIME 类型推断扩展名
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
