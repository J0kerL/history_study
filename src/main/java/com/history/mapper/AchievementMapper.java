package com.history.mapper;

import com.history.model.entity.Achievement;
import com.history.model.entity.UserAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 插入用户成就解锁记录（INSERT IGNORE 保证幂等，无需应用层预查重）。
     */
    @Insert("INSERT IGNORE INTO t_user_achievement (user_id, achievement_id, unlocked_at) VALUES (#{userId}, #{achievementId}, NOW())")
    int insertUserAchievement(@Param("userId") Long userId, @Param("achievementId") Integer achievementId);

    /**
     * 查询所有成就定义。
     */
    @Select("SELECT * FROM t_achievement ORDER BY condition_type, condition_value")
    java.util.List<com.history.model.entity.Achievement> selectAll();

    /**
     * 统计用户已解锁的成就数量。
     */
    @Select("SELECT COUNT(*) FROM t_user_achievement WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") long userId);

    /**
     * 查询用户已解锁的成就ID及解锁时间。
     *
     * @param userId 用户ID
     * @return achievementId -> unlockedAt 的映射
     */
    List<Map<String, Object>> selectUnlockedMapByUserId(@Param("userId") long userId);
}
