package com.history.service;

import com.history.model.vo.QuizAnswerResultVO;
import com.history.model.vo.QuizHistoryVO;
import com.history.model.vo.QuizStatsVO;
import com.history.model.vo.TodayQuizVO;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * 每日一题 Service。
 *
 * @author Diamond
 */
public interface QuizService {

    /**
     * 获取今日题目（不含答案）。userId 为 null 时表示匿名访问。
     */
    TodayQuizVO getTodayQuiz(@Nullable Long userId);

    /**
     * 提交答案，返回结果。
     */
    QuizAnswerResultVO submitAnswer(Long userId, Long quizId, String selectedOptions);

    /**
     * 查询用户学习统计。userId 为 null 时返回默认值。
     */
    QuizStatsVO getStats(@Nullable Long userId);

    /**
     * 查询历史答题记录。
     */
    List<QuizHistoryVO> getQuizHistory(Long userId, int limit);

    /**
     * 手动触发生成题库（批量）。
     */
    int generateQuizBatch(int count);
}
