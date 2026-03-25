package com.history.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.history.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常处理器，统一将异常转换为标准 Result 返回。
 *
 * @author Diamond
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int BAD_REQUEST_CODE = 400;
    private static final int UNAUTHORIZED_CODE = 401;
    private static final int FORBIDDEN_CODE = 403;
    private static final int METHOD_NOT_ALLOWED_CODE = 405;

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录或登录态无效: {}", e.getMessage());
        return Result.fail(UNAUTHORIZED_CODE, "未登录或登录状态已失效");
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(FORBIDDEN_CODE, "无权限访问该资源");
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足: {}", e.getMessage());
        return Result.fail(FORBIDDEN_CODE, "当前角色无权限访问该资源");
    }

    @ExceptionHandler(SaTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleSaTokenException(SaTokenException e) {
        log.warn("Sa-Token 异常: {}", e.getMessage());
        return Result.fail(UNAUTHORIZED_CODE, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数校验失败";
        return Result.fail(BAD_REQUEST_CODE, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return Result.fail(BAD_REQUEST_CODE, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "请求参数校验失败";
        }
        return Result.fail(BAD_REQUEST_CODE, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.fail(BAD_REQUEST_CODE, "缺少必要请求参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return Result.fail(BAD_REQUEST_CODE, "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return Result.fail(BAD_REQUEST_CODE, "请求体格式错误或缺失");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return Result.fail(METHOD_NOT_ALLOWED_CODE, "请求方法不支持: " + e.getMethod());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.fail(BAD_REQUEST_CODE, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(Result.FAIL_CODE, "系统开小差了，请稍后再试");
    }
}
