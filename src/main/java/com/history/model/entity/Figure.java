package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 历史人物实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "历史人物实体")
public class Figure implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 人物姓名。 */
    @Schema(description = "人物姓名")
    private String name;

    /** 人物副标题。 */
    @Schema(description = "人物副标题")
    private String subtitle;

    /** 出生日期描述。 */
    @Schema(description = "出生日期描述")
    private String birthDate;

    /** 逝世日期描述。 */
    @Schema(description = "逝世日期描述")
    private String deathDate;

    /** 籍贯或出生地。 */
    @Schema(description = "籍贯或出生地")
    private String birthplace;

    /** 人物详情内容。 */
    @Schema(description = "人物详情内容")
    private String content;

    /** 人物图片地址。 */
    @Schema(description = "人物图片地址")
    private String imageUrl;

    /** 标签，多个标签以逗号分隔。 */
    @Schema(description = "标签，多个标签以逗号分隔")
    private String tags;

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
