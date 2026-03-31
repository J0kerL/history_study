package com.history.mapper;

import com.history.model.entity.Event;
import com.history.model.vo.EventSummaryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventMapper {

    /**
     * 根据月日查询今日事件摘要列表。
     *
     * @param month 月份
     * @param day 日期
     * @return 事件摘要列表
     */
    List<EventSummaryVO> selectSummaryByMonthDay(@Param("month") int month, @Param("day") int day);

    /**
     * 根据事件ID查询事件详情。
     *
     * @param id 事件ID
     * @return 事件详情
     */
    Event selectById(@Param("id") Long id);

    /**
     * 查询关联事件摘要列表。
     *
     * @param eventId 源事件ID
     * @return 关联事件摘要列表
     */
    List<EventSummaryVO> selectRelatedSummaries(@Param("eventId") Long eventId);
}
