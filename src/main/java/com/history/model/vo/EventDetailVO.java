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
@Schema(description = "事件详情")
public class EventDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "事件ID")
    private Long id;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件发生年份")
    private Short year;

    @Schema(description = "事件发生月份")
    private Byte month;

    @Schema(description = "事件发生日期")
    private Byte day;

    @Schema(description = "事件摘要")
    private String summary;

    @Schema(description = "事件详细内容")
    private String content;

    @Schema(description = "封面图片地址")
    private String imageUrl;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "数据来源")
    private Byte source;

    @Schema(description = "关联事件列表")
    private List<EventSummaryVO> relatedEvents;

    @Schema(description = "关联事件状态：ready/generating")
    private String relatedEventsStatus;

    @Schema(description = "关联事件状态说明")
    private String relatedEventsMessage;
}
