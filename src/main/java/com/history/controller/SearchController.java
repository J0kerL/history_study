package com.history.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.history.common.Result;
import com.history.model.vo.SearchResultVO;
import com.history.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索 Controller。
 *
 * @author Diamond
 */
@RestController
@RequestMapping("/search")
@Tag(name = "搜索", description = "搜索事件、人物，热门搜索词，搜索历史")
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping
    @Operation(summary = "搜索", description = "根据关键词搜索历史事件和人物。需登录。")
    public Result<SearchResultVO> search(@RequestParam String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(searchService.search(keyword, userId));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门搜索词", description = "返回当前展示中的热门搜索关键词。支持匿名访问。")
    public Result<List<String>> getHotKeywords() {
        return Result.success(searchService.getHotKeywords());
    }

    @GetMapping("/history")
    @Operation(summary = "获取搜索历史", description = "返回当前用户最近的搜索记录")
    public Result<List<String>> getSearchHistory() {
        return Result.success(searchService.getSearchHistory(StpUtil.getLoginIdAsLong()));
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空搜索历史", description = "清空当前用户的所有搜索历史")
    public Result<Void> clearSearchHistory() {
        searchService.clearSearchHistory(StpUtil.getLoginIdAsLong());
        return Result.success(null);
    }
}
