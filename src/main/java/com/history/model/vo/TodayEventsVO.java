package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "今日事件结果")
public class TodayEventsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "今日事件列表")
    private List<EventSummaryVO> events;

    @Schema(description = "生成状态：ready/generating")
    private String generationStatus;

    @Schema(description = "状态说明")
    private String generationMessage;
}
