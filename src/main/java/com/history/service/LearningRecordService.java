package com.history.service;

/**
 * 用户学习记录 Service。
 *
 * @author Diamond
 */
public interface LearningRecordService {

    /**
     * 记录用户学习行为。
     *
     * @param userId     用户ID
     * @param actionType 行为类型：1=浏览史今, 2=阅读详情, 3=答题, 4=收藏, 5=搜索
     */
    void recordLearningAction(Long userId, Byte actionType);

    /**
     * 计算用户连续学习天数。
     *
     * @param userId 用户ID
     * @return 连续学习天数
     */
    int calculateStreakDays(Long userId);

    /**
     * 查询指定日期是否有学习记录。
     *
     * @param userId    用户ID
     * @param learnDate 学习日期
     * @return 是否存在记录
     */
    boolean hasLearningRecord(Long userId, java.time.LocalDate learnDate);

    /**
     * 查询用户最近的学习日期。
     *
     * @param userId 用户ID
     * @return 最近学习日期，如果没有则返回 null
     */
    java.time.LocalDate getLatestLearnDate(Long userId);
}
