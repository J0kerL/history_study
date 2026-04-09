package com.history.mapper;

import com.history.model.entity.Figure;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface FigureMapper {

    /**
     * 根据 ID 查询人物
     */
    Figure selectById(@Param("id") Long id);

    /**
     * 查询指定日期推荐的人物
     */
    Figure selectTodayRecommendation(@Param("date") LocalDate date);

    /**
     * 插入人物记录
     */
    int insert(Figure figure);

    /**
     * 查询未推荐过的人物列表（用于推荐）
     */
    List<Figure> selectUnrecommendedFigures(@Param("limit") int limit);

    /**
     * 统计人物总数
     */
    int countAll();

    /**
     * 统计未推荐过的人物数量
     */
    int countUnrecommendedFigures();

    /**
     * 查询所有人物
     */
    List<Figure> selectAllFigures();
}
