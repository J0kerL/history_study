package com.history.model.vo;

import com.history.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Diamond
 * @Create 2026/3/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录返回信息")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String accessToken;
    @Schema(description = "刷新令牌")
    private String refreshToken;
    @Schema(description = "用户信息")
    private User user;

}
