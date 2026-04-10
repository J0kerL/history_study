package com.history.mapper;

import com.history.model.entity.SearchHot;
import com.history.model.vo.EventSummaryVO;
import com.history.model.vo.FigureSearchVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 搜索 Mapper。
 *
 * @author Diamond
 */
public interface SearchMapper {

    // ===== 搜索 =====

    /** 全文搜索事件（使用 ngram 全文索引）。 */
    List<EventSummaryVO> searchEvents(@Param("keyword") String keyword, @Param("limit") int limit);

    /** 模糊搜索人物。 */
    List<FigureSearchVO> searchFigures(@Param("keyword") String keyword, @Param("limit") int limit);

    // ===== 热词 =====

    /** 查询展示中的热词，按排序权重降序。 */
    @Select("SELECT id, keyword, sort_order, status FROM t_search_hot WHERE status = 1 ORDER BY sort_order DESC")
    List<SearchHot> selectHotKeywords();

    // ===== 搜索历史 =====

    /** 查询用户最近的搜索历史，按 id 降序。 */
    @Select("SELECT keyword FROM t_user_search_history WHERE user_id = #{userId} ORDER BY id DESC LIMIT #{limit}")
    List<String> selectSearchHistory(@Param("userId") Long userId, @Param("limit") int limit);

    /** 删除用户某关键词的旧记录（去重用）。 */
    @Delete("DELETE FROM t_user_search_history WHERE user_id = #{userId} AND keyword = #{keyword}")
    int deleteByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 插入搜索历史。 */
    @Insert("INSERT INTO t_user_search_history (user_id, keyword) VALUES (#{userId}, #{keyword})")
    int insertSearchHistory(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 清空用户搜索历史。 */
    @Delete("DELETE FROM t_user_search_history WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Long userId);
}
