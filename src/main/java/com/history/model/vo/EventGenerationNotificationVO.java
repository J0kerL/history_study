package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "事件生成通知")
public class EventGenerationNotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "通知类型：today-events/related-events")
    private String type;

    @Schema(description = "状态：subscribed/ready/failed")
    private String status;

    @Schema(description = "状态说明")
    private String message;

    @Schema(description = "月份，今日事件通知时返回")
    private Integer month;

    @Schema(description = "日期，今日事件通知时返回")
    private Integer day;

    @Schema(description = "事件ID，关联事件通知时返回")
    private Long eventId;
}
