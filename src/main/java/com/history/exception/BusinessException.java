package com.history.exception;

import com.history.common.Result;
import lombok.Getter;

/**
 * 自定义业务异常，用于承载业务错误码和错误信息。
 *
 * @author Diamond
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(Result.FAIL_CODE, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        this(Result.FAIL_CODE, message, cause);
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
