package com.history.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置类，负责创建并注入 OSS 客户端 Bean。
 * OssProperties 中的占位符需在对应环境配置文件中替换为真实凭证。
 *
 * @author Diamond
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    /**
     * 根据配置属性构造 OSS 客户端，供全局注入使用。
     *
     * @param ossProperties OSS 配置属性
     * @return OSS 客户端实例
     */
    @Bean
    public OSS ossClient(OssProperties ossProperties) {
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }
}
