package com.history.controller;

import com.history.common.Result;
import com.history.model.entity.Event;
import com.history.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/27
 */
@RestController
@RequestMapping("/event")
@Tag(name = "事件管理", description = "事件管理相关接口")
public class EventController {

    @Resource
    private EventService eventService;

    @GetMapping("/todayEvents")
    @Operation(summary = "获取今日事件", description = "获取今日事件列表")
    public Result<List<Event>> getTodayEvents() {
        return Result.success(eventService.getTodayEvents());
    }

}
