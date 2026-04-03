package com.history.service;

import com.history.common.PageResult;
import com.history.model.dto.AchievementQueryDTO;
import com.history.model.entity.Achievement;

/**
 * 用户成就 Service。
 *
 * @author Diamond
 */
public interface AchievementService {

    /**
     * 分页查询当前登录用户的成就列表。
     *
     * @param userId    用户 ID
     * @param queryDTO  查询参数（页码/每页数量）
     * @return 分页结果（成就定义）
     */
    PageResult<Achievement> listAchievements(Long userId, AchievementQueryDTO queryDTO);

    /**
     * 为用户解锁一个成就（仅当尚未解锁时执行）。
     *
     * @param userId      用户 ID
     * @param achievementId 成就 ID
     */
    void unlockAchievement(Long userId, Integer achievementId);
}

