package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求参数
 *
 * @author Diamond
 */
@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "history_user")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456")
    private String confirmPassword;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "手机验证码", example = "123456")
    private String verificationCode;
}
