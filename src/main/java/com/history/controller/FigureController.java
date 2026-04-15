package com.history.controller;

import com.history.common.Result;
import com.history.model.vo.FigureDetailVO;
import com.history.service.FigureRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/figure")
@Tag(name = "人物管理", description = "历史人物相关接口")
public class FigureController {

    @Resource
    private FigureRecommendationService figureRecommendationService;

    @GetMapping("/{id}")
    @Operation(summary = "获取人物详情", description = "根据人物ID获取人物详情")
    public Result<FigureDetailVO> getFigureDetail(@PathVariable Long id) {
        return Result.success(figureRecommendationService.getFigureDetail(id));
    }
}
