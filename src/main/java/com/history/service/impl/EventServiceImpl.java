package com.history.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.history.mapper.EventMapper;
import com.history.model.entity.Event;
import com.history.service.EventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/27
 */
@Service
public class EventServiceImpl implements EventService {

    @Resource
    private EventMapper eventMapper;

    /**
     * 获取今日事件
     *
     * @return
     */
    @Override
    public List<Event> getTodayEvents() {
        // 1.获取今日日期 只需根据月日来查询事件
        DateTime now = DateUtil.date();
        int month = DateUtil.month(now) + 1;
        int day = DateUtil.dayOfMonth(now);

        // 2.查询今日事件
        List<Event> events = eventMapper.selectByMonthDay(month, day);

        // 3. 如果没有查询到今日事件，则调用大模型生成
        /*if (events == null || events.isEmpty()) {
            // TODO 调用大模型生成今日事件
            // 3.1. 调用LLM获取生成结果
            // 3.2. 解析并存入t_event（source=2）
            // 3.3. 返回存库后的数据
        }*/
        return events;
    }
}
