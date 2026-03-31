package com.history.controller;

import com.history.common.Result;
import com.history.model.dto.FavoriteDTO;
import com.history.model.dto.SetFavoriteStatusDTO;
import com.history.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏模块", description = "收藏模块相关接口")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /**
     * 添加收藏接口。
     */
    @PostMapping("/add")
    @Operation(summary = "添加收藏", description = "将指定事件或人物添加到当前用户收藏列表")
    public Result<String> addFavorite(@Valid @RequestBody FavoriteDTO favoriteDTO) {
        favoriteService.addFavorite(favoriteDTO);
        return Result.success();
    }

    /**
     * 取消收藏接口。
     */
    @PostMapping("/remove")
    @Operation(summary = "取消收藏", description = "将指定事件或人物从当前用户收藏列表中移除")
    public Result<String> removeFavorite(@Valid @RequestBody FavoriteDTO favoriteDTO) {
        favoriteService.removeFavorite(favoriteDTO);
        return Result.success();
    }

    /**
     * 查询是否已收藏接口。
     */
    @GetMapping("/status")
    @Operation(summary = "查询是否已收藏", description = "查询当前登录用户是否已收藏指定事件或人物")
    public Result<Boolean> hasFavorite(@Valid FavoriteDTO favoriteDTO) {
        return Result.success(favoriteService.hasFavorite(favoriteDTO));
    }

    /**
     * 设置收藏状态接口。
     */
    @PostMapping("/set-status")
    @Operation(summary = "设置收藏状态", description = "按目标状态设置当前用户对指定事件或人物的收藏状态，适用于前端单按钮切换场景")
    public Result<Boolean> setFavoriteStatus(@Valid @RequestBody SetFavoriteStatusDTO setFavoriteStatusDTO) {
        return Result.success(favoriteService.setFavoriteStatus(setFavoriteStatusDTO));
    }
}
