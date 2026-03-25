package com.history.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author Diamond
 * @Create 2026/3/24
 */
@Configuration
public class RedisTemplateConfig {
    /**
     * 核心Bean：用RedisTemplate（通用适配，支持多种数据类型，避免导入失败）
     * 配置RedisTemplate，用于操作Redis（替代StringRedisTemplate，支持多种数据类型）
     * 解决序列化问题：避免存储中文乱码、对象无法正常存储/读取
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 配置Redis连接工厂（适配Lettuce，Spring Boot 2.x/3.x默认，无需Jedis）
        redisTemplate.setConnectionFactory(factory);
        // 2. 配置序列化方式（关键，避免乱码）
        // 字符串序列化（key用字符串）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // JSON序列化（value用JSON，支持对象转换）
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 配置JSON序列化细节（避免序列化失败）
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jsonSerializer.setObjectMapper(objectMapper);
        // 给RedisTemplate设置序列化器
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        return redisTemplate;
    }
}
