package com.history.mapper;

import com.history.model.entity.Event;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/27
 */
public interface EventMapper {

    /**
     * 根据月份和日期查询历史事件（跨年份）
     *
     * @param month 月份（1-12）
     * @param day   日期（1-31）
     * @return 该月日发生的历史事件列表，按年份升序排列
     */
    List<Event> selectByMonthDay(@Param("month") int month, @Param("day") int day);

}
