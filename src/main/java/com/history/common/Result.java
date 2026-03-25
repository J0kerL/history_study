package com.history.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一接口返回结果。
 *
 * @author Diamond
 * @param <T> 返回数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码。
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 默认失败状态码。
     */
    public static final int FAIL_CODE = 500;

    /**
     * 业务状态码。
     */
    private Integer code;

    /**
     * 返回消息。
     */
    private String message;

    /**
     * 返回数据。
     */
    private T data;

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    public static <T> Result<T> fail() {
        return new Result<>(FAIL_CODE, "fail", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(FAIL_CODE, message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
