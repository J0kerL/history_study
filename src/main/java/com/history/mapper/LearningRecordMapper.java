package com.history.mapper;

import com.history.model.entity.UserLearningRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户学习记录 Mapper。
 *
 * @author Diamond
 */
public interface LearningRecordMapper {

    /**
     * 插入学习记录（幂等：同一天同一行为只记录一次）。
     *
     * @param userId     用户ID
     * @param learnDate  学习日期
     * @param actionType 行为类型
     * @return 影响行数
     */
    @Insert("""
            INSERT IGNORE INTO t_user_learning_record (user_id, learn_date, action_type)
            VALUES (#{userId}, #{learnDate}, #{actionType})
            """)
    int insertRecord(@Param("userId") Long userId,
                     @Param("learnDate") LocalDate learnDate,
                     @Param("actionType") Byte actionType);

    /**
     * 查询指定日期是否有学习记录。
     *
     * @param userId    用户ID
     * @param learnDate 学习日期
     * @return 是否存在记录
     */
    @Select("""
            SELECT COUNT(*) > 0
            FROM t_user_learning_record
            WHERE user_id = #{userId} AND learn_date = #{learnDate}
            """)
    boolean hasLearningRecord(@Param("userId") Long userId,
                              @Param("learnDate") LocalDate learnDate);

    /**
     * 查询用户最近的学习日期。
     *
     * @param userId 用户ID
     * @return 最近学习日期，如果没有则返回 null
     */
    @Select("""
            SELECT MAX(learn_date)
            FROM t_user_learning_record
            WHERE user_id = #{userId}
            """)
    LocalDate getLatestLearnDate(@Param("userId") Long userId);

    /**
     * 批量查询用户最近 N 天的去重学习日期（降序），用于内存计算连续天数。
     * 取 400 天足以覆盖业务上限（365天连续学习）。
     *
     * @param userId 用户ID
     * @param limit  最多返回条数
     * @return 学习日期列表（降序）
     */
    @Select("""
            SELECT DISTINCT learn_date
            FROM t_user_learning_record
            WHERE user_id = #{userId}
            ORDER BY learn_date DESC
            LIMIT #{limit}
            """)
    List<LocalDate> selectRecentLearnDates(@Param("userId") Long userId, @Param("limit") int limit);
}
