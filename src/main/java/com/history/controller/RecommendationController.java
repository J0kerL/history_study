package com.history.controller;

import com.history.common.Result;
import com.history.model.vo.DailyRecommendationVO;
import com.history.service.FigureRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 每日推荐 Controller。
 *
 * @author Diamond
 */
@Slf4j
@RestController
@RequestMapping("/recommendation")
@Tag(name = "每日推荐", description = "每日推荐相关接口")
public class RecommendationController {

    @Resource
    private FigureRecommendationService figureRecommendationService;

    @GetMapping("/today")
    @Operation(summary = "获取今日推荐人物", description = "返回今日推荐的历史人物详情，如不存在则自动生成")
    public Result<DailyRecommendationVO> getTodayRecommendation() {
        return Result.success(figureRecommendationService.getTodayRecommendation());
    }
}
