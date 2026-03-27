package com.history.service;

import com.history.model.entity.Event;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/27
 */
public interface EventService {

    /**
     * 获取今日事件
     * @return
     */
    List<Event> getTodayEvents();

}
