package com.history.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.history.common.Result;
import com.history.model.vo.QuizAnswerResultVO;
import com.history.model.vo.QuizHistoryVO;
import com.history.model.vo.QuizStatsVO;
import com.history.model.vo.TodayQuizVO;
import com.history.model.dto.QuizAnswerDTO;
import com.history.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
@Tag(name = "每日一题管理", description = "每日一题答题相关接口")
public class QuizController {

    @Resource
    private QuizService quizService;

    @GetMapping("/today")
    @Operation(summary = "获取每日一题", description = "获取今日题目，不含正确答案和解析")
    public Result<TodayQuizVO> getTodayQuiz() {
        return Result.success(quizService.getTodayQuiz(StpUtil.getLoginIdAsLong()));
    }

    @PostMapping("/answer")
    @Operation(summary = "提交答题", description = "提交今日答题答案，返回结果与解析")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "答题请求")
    public Result<QuizAnswerResultVO> submitAnswer(@RequestParam Long quizId,
                                                @RequestBody QuizAnswerDTO dto) {
        return Result.success(quizService.submitAnswer(StpUtil.getLoginIdAsLong(), quizId, dto.getSelectedOptions()));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取学习统计", description = "获取用户连续学习天数、答题正确率等学习统计")
    public Result<QuizStatsVO> getStats() {
        return Result.success(quizService.getStats(StpUtil.getLoginIdAsLong()));
    }

    @GetMapping("/history")
    @Operation(summary = "获取答题记录", description = "获取用户历史答题记录（最近 50 条）")
    public Result<List<QuizHistoryVO>> getQuizHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(quizService.getQuizHistory(StpUtil.getLoginIdAsLong(), Math.min(limit, 100)));
    }

    @PostMapping("/generate-pool")
    @Operation(summary = "手动生成题库", description = "手动触发 LLM 批量生成题目，补充题库")
    public Result<Integer> generateQuizPool(
            @RequestParam(defaultValue = "100") int count) {
        return Result.success(quizService.generateQuizBatch(count));
    }
}
