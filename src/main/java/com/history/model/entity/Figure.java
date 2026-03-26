package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

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

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "人物姓名")
    private String name;

    @Schema(description = "人物副标题")
    private String subtitle;

    @Schema(description = "出生日期描述")
    private String birthDate;

    @Schema(description = "逝世日期描述")
    private String deathDate;

    @Schema(description = "籍贯或出生地")
    private String birthplace;

    @Schema(description = "人物详细内容")
    private String content;

    @Schema(description = "人物图片地址")
    private String imageUrl;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "状态：0-下线，1-正常")
    private Byte status;

    @Schema(description = "数据来源：1-人工录入，2-AI生成")
    private Byte source;
}
