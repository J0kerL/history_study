package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求参数。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求参数")
public class UpdatePasswordDTO {

    /** 原密码：用于身份验证，不能为空。 */
    @NotBlank(message = "请输入原密码")
    @Schema(description = "原密码")
    private String oldPassword;

    /** 新密码：6～20 个字符，不能为空。 */
    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 20, message = "密码长度应在 6～20 个字符之间")
    @Schema(description = "新密码")
    private String newPassword;

    /** 确认新密码：必须与新密码一致，不能为空。 */
    @NotBlank(message = "请再次输入新密码")
    @Schema(description = "确认新密码")
    private String confirmNewPassword;
}
