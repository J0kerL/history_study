package com.history.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.history.exception.BusinessException;
import com.history.mapper.EventMapper;
import com.history.model.entity.Event;
import com.history.model.vo.EventDetailVO;
import com.history.model.vo.EventSummaryVO;
import com.history.service.EventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Resource
    private EventMapper eventMapper;

    @Override
    public List<EventSummaryVO> getTodayEvents() {
        // 1.获取今日日期
        DateTime now = DateUtil.date();
        int month = DateUtil.month(now) + 1;
        int day = DateUtil.dayOfMonth(now);

        // 2.根据月日来查询今日事件
        List<EventSummaryVO> events = eventMapper.selectSummaryByMonthDay(month, day);

        // 3.如果数据库中没有今日事件，则调用大模型生成今日事件并落库 t_event，后续直接返回库中数据。
        /*if (events == null || events.isEmpty()) {
            // TODO 调用大模型生成今日事件并落库 t_event，摘要最多40字，后续直接返回库中数据。
        }*/
        return events;
    }

    @Override
    public EventDetailVO getEventDetail(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        // 1.获取事件关联事件列表
        List<EventSummaryVO> relatedEvents = eventMapper.selectRelatedSummaries(id);
        // 2.如果数据库中没有关联事件，则返回空列表，并接入大模型生成关联事件
        if (relatedEvents == null || relatedEvents.isEmpty()) {
            relatedEvents = Collections.emptyList();
            // TODO 接入大模型：先基于当前事件 tags 召回候选事件，再由大模型做筛选、排序并落库 t_event_relation。
        }

        return new EventDetailVO(
                event.getId(),
                event.getTitle(),
                event.getYear(),
                event.getMonth(),
                event.getDay(),
                event.getSummary(),
                event.getContent(),
                event.getImageUrl(),
                event.getTags(),
                event.getSource(),
                relatedEvents
        );
    }
}
