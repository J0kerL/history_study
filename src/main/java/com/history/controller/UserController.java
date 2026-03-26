package com.history.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.history.common.PageResult;
import com.history.common.Result;
import com.history.model.dto.AchievementQueryDTO;
import com.history.model.dto.FavoriteQueryDTO;
import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.Achievement;
import com.history.model.entity.User;
import com.history.model.vo.FavoriteVO;
import com.history.service.AchievementService;
import com.history.service.FavoriteService;
import com.history.service.OssService;
import com.history.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Resource
    private OssService ossService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private AchievementService achievementService;

    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户信息")
    public Result<User> getCurrentUserInfo() {
        // 获取登录用户id
        long id = StpUtil.getLoginIdAsLong();
        User user = userService.getCurrentUserInfo(id);
        return Result.success(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户个人信息", description = "动态更新用户名、密码、手机号，仅填写的字段生效。如需修改头像请使用 POST /user/avatar 接口")
    public Result<User> update(@Valid @RequestBody UpdateUserProfileDTO updateProfileDTO) {
        long id = StpUtil.getLoginIdAsLong();
        User user = userService.update(id, updateProfileDTO);
        return Result.success(user);
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "将图片文件上传至阿里云 OSS，自动更新用户头像地址。支持 JPG、PNG、GIF、WEBP 格式，最大 5 MB")
    public Result<User> uploadAvatar(@RequestParam("file") MultipartFile file) {
        long id = StpUtil.getLoginIdAsLong();
        // 上传图片到 OSS，得到访问 URL
        String avatarUrl = ossService.uploadAvatar(id, file);
        // 将 URL 保存到数据库，并返回更新后的用户信息
        User user = userService.updateAvatar(id, avatarUrl);
        return Result.success(user);
    }

    @GetMapping("/achievements")
    @Operation(summary = "获取用户成就列表", description = "分页查询当前登录用户的成就列表")
    public Result<PageResult<Achievement>> listAchievements(@Valid AchievementQueryDTO queryDTO) {
        long id = StpUtil.getLoginIdAsLong();
        PageResult<Achievement> pageResult = achievementService.listAchievements(id, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/favorites")
    @Operation(summary = "获取用户收藏列表", description = "分页查询当前登录用户的收藏列表，支持按收藏类型筛选")
    public Result<PageResult<FavoriteVO>> listFavorites(@Valid FavoriteQueryDTO queryDTO) {
        long id = StpUtil.getLoginIdAsLong();
        PageResult<FavoriteVO> favorites = favoriteService.listFavorites(id, queryDTO);
        return Result.success(favorites);
    }
}
