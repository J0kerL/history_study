package com.history.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP 请求日志拦截器。
 * <p>
 * 在请求进入 Controller 前打印"请求开始"日志，响应完成后打印"请求结束"日志，
 * 包含 HTTP 方法、URI、Controller 方法名、耗时和响应状态码，方便开发调试。
 *
 * @author Diamond
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    /** 请求开始时间存放的 Request 属性名 */
    private static final String START_TIME_ATTR = "reqStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        if (handler instanceof HandlerMethod handlerMethod) {
            // 记录控制器类名和方法名，便于快速定位代码
            String className  = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            log.info("→ {} {}  [{}.{}()]",
                    request.getMethod(), request.getRequestURI(), className, methodName);
        } else {
            log.info("→ {} {}", request.getMethod(), request.getRequestURI());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long duration  = (startTime != null) ? System.currentTimeMillis() - startTime : -1;

        if (ex != null) {
            // 请求处理过程中抛出了未被捕获的异常
            log.info("← {} {}  {}ms  [{}]  ✗ {}",
                    request.getMethod(), request.getRequestURI(),
                    duration, response.getStatus(), ex.getMessage());
        } else {
            log.info("← {} {}  {}ms  [{}]",
                    request.getMethod(), request.getRequestURI(),
                    duration, response.getStatus());
        }
    }
}
