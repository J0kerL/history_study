package com.history.controller;

import com.history.common.Result;
import com.history.model.dto.AddFavoriteDTO;
import com.history.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

    @PostMapping("/add")
    @Operation(summary = "添加收藏", description = "添加收藏")
    public Result<String> addFavorite(@Valid @RequestBody AddFavoriteDTO addFavoriteDTO) {
        favoriteService.addFavorite(addFavoriteDTO);
        return Result.success();
    }
}
