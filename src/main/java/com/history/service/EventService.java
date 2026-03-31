package com.history.service;

import com.history.model.vo.EventDetailVO;
import com.history.model.vo.EventSummaryVO;

import java.util.List;

public interface EventService {

    /**
     * 获取今日事件列表。
     *
     * @return 事件列表
     */
    List<EventSummaryVO> getTodayEvents();

    /**
     * 获取事件详情及关联事件。
     *
     * @param id 事件ID
     * @return 事件详情
     */
    EventDetailVO getEventDetail(Long id);
}
