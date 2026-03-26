package com.history.mapper;

import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
