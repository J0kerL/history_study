package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户个人信息请求参数。
 * 所有字段均为可选项，只有填写的字段才会被更新。
 * 注意：头像更新请使用 POST /user/avatar 接口上传图片文件。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户信息请求参数")
public class UpdateUserProfileDTO {

    /** 用户名：1～20 个字符，留空表示不修改。 */
    @Size(min = 1, max = 20, message = "用户名长度应在 1～20 个字符之间")
    @Schema(description = "用户名")
    private String username;

    /**
     * 新密码：6～20 个字符，留空表示不修改。
     * 该字段接收明文密码，Service 层负责加密存储。
     */
    @Size(min = 6, max = 20, message = "密码长度应在 6～20 个字符之间")
    @Schema(description = "密码")
    private String password;

    /** 手机号：中国大陆 11 位数字，留空表示不修改。 */
    @Pattern(regexp = "^\\d{11}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;
}
