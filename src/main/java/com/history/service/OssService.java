package com.history.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 文件存储服务接口。
 *
 * @author Diamond
 */
public interface OssService {

    /**
     * 将头像文件上传至阿里云 OSS。
     *
     * @param userId 当前登录用户 ID，用于生成唯一存储路径
     * @param file   上传的图片文件（仅支持 jpg、png、gif、webp）
     * @return 上传成功后文件的公网访问 URL
     */
    String uploadAvatar(long userId, MultipartFile file);
}
