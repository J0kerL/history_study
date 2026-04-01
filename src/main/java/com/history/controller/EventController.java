package com.history.controller;

import com.history.common.Result;
import com.history.model.vo.EventDetailVO;
import com.history.model.vo.TodayEventsVO;
import com.history.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
@Tag(name = "事件管理", description = "事件相关接口")
public class EventController {

    @Resource
    private EventService eventService;

    @GetMapping("/todayEvents")
    @Operation(summary = "获取今日事件", description = "返回今日事件列表和生成状态")
    public Result<TodayEventsVO> getTodayEvents() {
        return Result.success(eventService.getTodayEvents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取事件详情", description = "根据事件ID获取完整事件信息及关联事件状态")
    public Result<EventDetailVO> getEventDetail(@PathVariable Long id) {
        return Result.success(eventService.getEventDetail(id));
    }
}
