package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送手机验证码请求参数
 *
 * @author Diamond
 */
@Data
@Schema(description = "发送手机验证码请求参数")
public class SendVerificationCodeDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
