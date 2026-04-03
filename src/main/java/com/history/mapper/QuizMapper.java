package com.history.mapper;

import com.history.model.entity.Quiz;
import com.history.model.entity.UserQuizRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Diamond
 */
public interface QuizMapper {

    /**
     * 查询题库总数。
     */
    @Select("SELECT COUNT(*) FROM t_quiz WHERE status = 1")
    int countQuiz();

    /**
     * 查询未被 t_daily_quiz 使用过的题目数量。
     */
    @Select("""
            SELECT COUNT(*) FROM t_quiz q
            LEFT JOIN t_daily_quiz dq ON q.id = dq.quiz_id
            WHERE q.status = 1 AND dq.quiz_id IS NULL
            """)
    int countUnusedQuiz();

    /**
     * 批量插入题库。
     */
    int batchInsert(@Param("quizzes") List<Quiz> quizzes);

    /**
     * 根据难度查询一道未被选入每日一题的题目（随机）。
     *
     * @param difficulty 难度（0 表示不限难度）
     */
    Quiz selectUnusedQuiz(@Param("difficulty") Byte difficulty);

    /**
     * 根据 ID 查询题目。
     */
    @Select("SELECT * FROM t_quiz WHERE id = #{id}")
    Quiz selectById(long id);

    /**
     * 查询今日题目。
     */
    @Select("SELECT dq.quiz_id FROM t_daily_quiz dq WHERE dq.quiz_date = #{date}")
    Long selectTodayQuizByDate(LocalDate date);

    /**
     * 插入每日题目绑定记录。
     */
    @Insert("INSERT INTO t_daily_quiz (quiz_date, quiz_id) VALUES (#{quizDate}, #{quizId})")
    int insertDailyQuiz(@Param("quizDate") LocalDate quizDate, @Param("quizId") Long quizId);

    /**
     * 查询用户今日是否已答题。
     */
    @Select("SELECT * FROM t_user_quiz_record WHERE user_id = #{userId} AND answer_date = #{date}")
    UserQuizRecord selectUserTodayRecord(@Param("userId") long userId, @Param("date") LocalDate date);

    /**
     * 插入答题记录。
     */
    @Insert("""
            INSERT INTO t_user_quiz_record (user_id, quiz_id, selected_options, is_correct, answer_date)
            VALUES (#{userId}, #{quizId}, #{selectedOptions}, #{isCorrect}, #{answerDate})
            """)
    int insertQuizRecord(UserQuizRecord record);

    /**
     * 查询用户最近 N 条答题记录的正确标志。
     */
    @Select("SELECT is_correct FROM t_user_quiz_record WHERE user_id = #{userId} ORDER BY answer_date DESC LIMIT #{limit}")
    List<Byte> selectRecentCorrectFlags(@Param("userId") long userId, @Param("limit") int limit);

    /**
     * 查询用户历史答题记录。
     */
    List<Map<String, Object>> selectUserQuizHistory(@Param("userId") long userId, @Param("limit") int limit);

    /**
     * 查询用户最近一次答题的难度。
     */
    @Select("""
            SELECT q.difficulty FROM t_user_quiz_record r
            JOIN t_quiz q ON r.quiz_id = q.id
            WHERE r.user_id = #{userId}
            ORDER BY r.answer_date DESC LIMIT 1
            """)
    Byte selectLastDifficulty(long userId);
}
