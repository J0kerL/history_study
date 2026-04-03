package com.history.service;

import com.history.model.vo.QuizAnswerResultVO;
import com.history.model.vo.QuizHistoryVO;
import com.history.model.vo.QuizStatsVO;
import com.history.model.vo.TodayQuizVO;

import java.util.List;

/**
 * 每日一题 Service。
 *
 * @author Diamond
 */
public interface QuizService {

    /**
     * 获取今日题目（不含答案）。
     */
    TodayQuizVO getTodayQuiz(Long userId);

    /**
     * 提交答案，返回结果。
     */
    QuizAnswerResultVO submitAnswer(Long userId, Long quizId, String selectedOptions);

    /**
     * 查询用户学习统计。
     */
    QuizStatsVO getStats(Long userId);

    /**
     * 查询历史答题记录。
     */
    List<QuizHistoryVO> getQuizHistory(Long userId, int limit);

    /**
     * 手动触发生成题库（批量）。
     */
    int generateQuizBatch(int count);
}
