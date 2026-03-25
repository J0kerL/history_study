package com.history.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.history.common.Result;
import com.history.model.entity.User;
import com.history.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
