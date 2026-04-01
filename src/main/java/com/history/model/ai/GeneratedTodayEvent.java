package com.history.model.ai;

import lombok.Data;

/**
 * AI 生成的今日事件条目。
 */
@Data
public class GeneratedTodayEvent {

    private String title;

    private Integer year;

    private String summary;

    private String content;

    private String tags;
}
