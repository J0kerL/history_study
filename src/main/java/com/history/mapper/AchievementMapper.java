package com.history.mapper;

import com.history.model.entity.Achievement;
import com.history.model.entity.UserAchievement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/26
 */
public interface AchievementMapper {

    Achievement selectById(Integer achievementId);

    /**
     * 批量根据成就 id 查询成就定义。
     *
     * @param achievementIds 成就 id 列表
     * @return 成就定义列表（仅包含命中记录）
     */
    List<Achievement> selectByIds(@Param("achievementIds") List<Integer> achievementIds);

    List<UserAchievement> selectByUserId(long id);
}
