package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 事件标题。 */
    @Schema(description = "事件标题")
    private String title;

    /** 事件发生年份。 */
    @Schema(description = "事件发生年份")
    private Short year;

    /** 事件发生月份。 */
    @Schema(description = "事件发生月份")
    private Byte month;

    /** 事件发生日期。 */
    @Schema(description = "事件发生日期")
    private Byte day;

    /** 事件摘要。 */
    @Schema(description = "事件摘要")
    private String summary;

    /** 事件详情内容。 */
    @Schema(description = "事件详情内容")
    private String content;

    /** 封面图片地址。 */
    @Schema(description = "封面图片地址")
    private String imageUrl;

    /** 标签，多个标签以逗号分隔。 */
    @Schema(description = "标签，多个标签以逗号分隔")
    private String tags;

    /** 浏览次数。 */
    @Schema(description = "浏览次数")
    private Integer viewCount;

    /** 状态：0-下线，1-正常。 */
    @Schema(description = "状态：0-下线，1-正常")
    private Byte status;

    /** 数据来源：1-人工录入，2-大模型生成。 */
    @Schema(description = "数据来源：1-人工录入，2-大模型生成")
    private Byte source;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
