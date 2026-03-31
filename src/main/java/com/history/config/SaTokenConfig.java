package com.history.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Diamond
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Resource
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 请求日志拦截器（先注册先执行，覆盖所有路径，无需排除）
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**");

        // 2. Sa-Token 登录校验拦截器
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/refreshToken",
                        "/auth/register",
                        "/auth/send-verification-code",
                        "/event/todayEvents",
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
