package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 历史事件实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "历史事件实体")
public class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
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

    @Schema(description = "数据来源：1-人工录入，2-AI生成")
    private Byte source;
}
