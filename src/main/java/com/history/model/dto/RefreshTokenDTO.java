package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Diamond
 */
@Data
@Schema(description = "刷新 Token 请求参数")
public class RefreshTokenDTO {

    @NotBlank(message = "refreshToken不能为空")
    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
