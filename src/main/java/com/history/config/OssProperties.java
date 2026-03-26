package com.history.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 阿里云 OSS 配置属性，绑定 application.yml 中 oss.* 前缀的配置项。
 *
 * @author Diamond
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** OSS 服务节点 */
    private String endpoint;

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucketName;

    /** 文件访问地址前缀，格式：https://<bucket>.<endpoint> */
    private String urlPrefix;

    /** 头像在 Bucket 中的目录前缀，默认 avatar */
    private String avatarDir = "avatar";

    /** 单文件大小上限，单位：MB，默认 5 MB */
    private long maxFileSize = 5;

    /** 允许上传的图片 MIME 类型列表 */
    private List<String> allowedContentTypes;
}
