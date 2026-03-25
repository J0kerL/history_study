package com.history.mapper;

import com.history.model.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
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
}
