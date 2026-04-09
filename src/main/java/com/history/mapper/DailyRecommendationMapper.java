package com.history.mapper;

import com.history.model.entity.DailyRecommendation;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 每日推荐 Mapper。
 *
 * @author Diamond
 */
public interface DailyRecommendationMapper {

    /**
     * 查询指定日期的推荐记录
     */
    DailyRecommendation selectByDate(@Param("date") LocalDate date);

    /**
     * 插入推荐记录
     */
    int insert(DailyRecommendation recommendation);
}
