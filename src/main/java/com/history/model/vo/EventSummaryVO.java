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
@Schema(description = "事件摘要")
public class EventSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "事件ID")
    private Long id;

    @Schema(description = "事件发生年份")
    private Short year;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件摘要")
    private String summary;

    @Schema(description = "封面图片地址")
    private String imageUrl;

}
