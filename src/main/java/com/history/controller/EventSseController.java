package com.history.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.history.service.impl.EventSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/event/sse")
@Tag(name = "事件SSE通知", description = "事件生成实时通知接口")
public class EventSseController {

    @Resource
    private EventSseService eventSseService;

    @GetMapping(value = "/todayEvents", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅今日事件生成通知", description = "订阅当天或指定月日的今日事件生成完成通知")
    public SseEmitter subscribeTodayEvents(@RequestParam(required = false) Integer month,
                                           @RequestParam(required = false) Integer day) {
        if (month == null || day == null) {
            DateTime now = DateUtil.date();
            month = DateUtil.month(now) + 1;
            day = DateUtil.dayOfMonth(now);
        }
        return eventSseService.subscribeTodayEvents(month, day);
    }

    @GetMapping(value = "/relatedEvents/{eventId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅关联事件生成通知", description = "订阅指定事件的关联事件生成完成通知")
    public SseEmitter subscribeRelatedEvents(@PathVariable Long eventId) {
        return eventSseService.subscribeRelatedEvents(eventId);
    }
}
