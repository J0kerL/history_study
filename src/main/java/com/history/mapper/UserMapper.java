package com.history.mapper;

import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * @Author Diamond
 * @Create 2026/3/24
 */
public interface UserMapper {
    @Select("select * from t_user where username = #{username}")
    User selectByUsername(String username);

    @Select("select * from t_user where phone = #{phone}")
    User selectByPhone(String phone);

    @Insert("""
            insert into t_user (
                username,
                password,
                phone,
                avatar,
                register_date,
                streak_days,
                max_streak_days,
                total_quiz_count,
                correct_quiz_count,
                total_favorite_count,
                status
            ) values (
                #{username},
                #{password},
                #{phone},
                #{avatar},
                #{registerDate},
                #{streakDays},
                #{maxStreakDays},
                #{totalQuizCount},
                #{correctQuizCount},
                #{totalFavoriteCount},
                #{status}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("select * from t_user where id = #{id}")
    User getCurrentUserInfo(long id);

    @Select("select * from t_user where id = #{id}")
    User selectById(long id);


    /**
     * 动态更新用户信息。
     *
     * @param id              用户主键
     * @param updateProfileDTO 待更新字段（仅非空字段生效，password 须已完成 BCrypt 加密）
     */
    void update(@Param("id") Long id, @Param("dto") UpdateUserProfileDTO updateProfileDTO);

    /**
     * 更新用户头像地址。
     *
     * @param id        用户主键
     * @param avatarUrl OSS 头像访问 URL
     */
    void updateAvatar(@Param("id") long id, @Param("avatarUrl") String avatarUrl);

    /**
     * 更新用户密码
     *
     * @param id       用户主键
     * @param password BCrypt 加密后的新密码
     */
    @Update("UPDATE t_user SET password = #{password} WHERE id = #{id}")
    void updatePassword(@Param("id") long id, @Param("password") String password);

    /**
     * 更新用户答题统计数据。
     */
    @Update("""
            UPDATE t_user SET
                total_quiz_count = #{totalQuizCount},
                correct_quiz_count = #{correctQuizCount},
                streak_days = #{streakDays},
                max_streak_days = #{maxStreakDays}
            WHERE id = #{id}
            """)
    void updateUserQuizStats(@Param("id") Long id,
                             @Param("totalQuizCount") int totalQuizCount,
                             @Param("correctQuizCount") int correctQuizCount,
                             @Param("streakDays") int streakDays,
                             @Param("maxStreakDays") int maxStreakDays);

    /**
     * 仅更新用户答题计数（不含连续天数，由 LearningRecordService 统一管理）。
     */
    @Update("""
            UPDATE t_user SET
                total_quiz_count = #{totalQuizCount},
                correct_quiz_count = #{correctQuizCount}
            WHERE id = #{id}
            """)
    void updateUserQuizCounts(@Param("id") Long id,
                              @Param("totalQuizCount") int totalQuizCount,
                              @Param("correctQuizCount") int correctQuizCount);

    /**
     * 更新用户连续学习天数。
     */
    @Update("UPDATE t_user SET streak_days = #{streakDays} WHERE id = #{id}")
    void updateStreakDays(@Param("id") Long id, @Param("streakDays") int streakDays);

    /**
     * 更新用户收藏总数。
     */
    @Update("UPDATE t_user SET total_favorite_count = #{totalFavoriteCount} WHERE id = #{id}")
    void updateTotalFavoriteCount(@Param("id") Long id, @Param("totalFavoriteCount") int totalFavoriteCount);
}
