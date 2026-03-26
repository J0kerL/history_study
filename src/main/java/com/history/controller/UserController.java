package com.history.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.history.common.Result;
import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;
import com.history.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Diamond
 * @Create 2026/3/25
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户信息")
    public Result<User> getCurrentUserInfo() {
        // 获取登录用户id
        long id = StpUtil.getLoginIdAsLong();
        User user = userService.getCurrentUserInfo(id);
        return Result.success(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户个人信息", description = "动态更新用户名、密码、手机号、头像，仅填写的字段生效")
    public Result<User> update(@Valid @RequestBody UpdateUserProfileDTO updateProfileDTO) {
        long id = StpUtil.getLoginIdAsLong();
        User user = userService.update(id, updateProfileDTO);
        return Result.success(user);
    }
}
