package com.history.service;

import com.history.model.vo.EventDetailVO;
import com.history.model.vo.TodayEventsVO;

public interface EventService {

    TodayEventsVO getTodayEvents();

    EventDetailVO getEventDetail(Long id);
}
