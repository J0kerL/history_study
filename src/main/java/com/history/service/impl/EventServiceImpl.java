package com.history.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.history.exception.BusinessException;
import com.history.mapper.EventMapper;
import com.history.model.entity.Event;
import com.history.model.vo.EventDetailVO;
import com.history.model.vo.EventSummaryVO;
import com.history.model.vo.TodayEventsVO;
import com.history.service.EventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private static final int TODAY_EVENT_LIMIT = 5;
    private static final int RELATED_EVENT_LIMIT = 3;

    @Resource
    private EventMapper eventMapper;

    @Resource
    private EventAiAsyncService eventAiAsyncService;

    @Override
    public TodayEventsVO getTodayEvents() {
        DateTime now = DateUtil.date();
        int month = DateUtil.month(now) + 1;
        int day = DateUtil.dayOfMonth(now);

        List<EventSummaryVO> events = deduplicateSummaries(eventMapper.selectSummaryByMonthDay(month, day), TODAY_EVENT_LIMIT);
        String generationStatus = "ready";
        String generationMessage = "今日事件已就绪";

        if (events.isEmpty()) {
            log.info("今日事件为空，异步触发大模型生成: month={}, day={}", month, day);
            eventAiAsyncService.generateTodayEventsAsync(month, day);
            generationStatus = "generating";
            generationMessage = "今日事件生成中";
        } else if (events.size() < TODAY_EVENT_LIMIT) {
            log.info("今日事件存在缺口或去重后不足，异步补齐: month={}, day={}, count={}", month, day, events.size());
            eventAiAsyncService.generateTodayEventsAsync(month, day);
            if (eventAiAsyncService.isTodayGenerationRunning(month, day)) {
                generationStatus = "generating";
                generationMessage = "今日事件补充生成中";
            }
        } else {
            log.info("命中今日事件数据: month={}, day={}, count={}", month, day, events.size());
        }

        if (events.isEmpty() && eventAiAsyncService.isTodayGenerationRunning(month, day)) {
            generationStatus = "generating";
            generationMessage = "今日事件生成中";
        }

        log.info("返回今日事件列表: month={}, day={}, count={}, status={}",
                month, day, events.size(), generationStatus);
        return new TodayEventsVO(events, generationStatus, generationMessage);
    }

    @Override
    public EventDetailVO getEventDetail(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        List<EventSummaryVO> relatedEvents = deduplicateSummaries(eventMapper.selectRelatedSummaries(id), RELATED_EVENT_LIMIT);
        String relatedEventsStatus = "ready";
        String relatedEventsMessage = "关联事件已就绪";

        if (relatedEvents.isEmpty()) {
            log.info("事件缺少关联数据，异步触发大模型推荐: eventId={}", id);
            eventAiAsyncService.generateRelatedEventsAsync(event);
            relatedEventsStatus = "generating";
            relatedEventsMessage = "关联事件生成中";
        } else {
            log.info("命中事件关联数据: eventId={}, count={}", id, relatedEvents.size());
        }

        if (relatedEvents.isEmpty() && eventAiAsyncService.isRelatedGenerationRunning(id)) {
            relatedEventsStatus = "generating";
            relatedEventsMessage = "关联事件生成中";
        }

        log.info("返回事件详情: eventId={}, relatedCount={}, relatedStatus={}",
                id, relatedEvents.size(), relatedEventsStatus);
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
                relatedEvents,
                relatedEventsStatus,
                relatedEventsMessage
        );
    }

    private List<EventSummaryVO> deduplicateSummaries(List<EventSummaryVO> events, int limit) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        Map<String, EventSummaryVO> uniqueEvents = new LinkedHashMap<>();
        for (EventSummaryVO event : events) {
            if (event == null || event.getId() == null || StrUtil.isBlank(event.getTitle())) {
                continue;
            }
            String key = event.getYear() + "::" + StrUtil.trim(event.getTitle()).replaceAll("\\s+", "");
            uniqueEvents.putIfAbsent(key, event);
            if (uniqueEvents.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(uniqueEvents.values());
    }
}
