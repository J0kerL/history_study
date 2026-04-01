package com.history.model.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 生成的今日事件结果。
 */
@Data
public class TodayEventsGenerationResult {

    private List<GeneratedTodayEvent> events;
}
