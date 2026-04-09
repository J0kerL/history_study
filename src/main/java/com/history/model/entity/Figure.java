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

    @Schema(description = "人物ID")
    private Long id;

    @Schema(description = "人物姓名")
    private String name;

    @Schema(description = "人物副标题")
    private String subtitle;

    @Schema(description = "出生日期描述")
    private String birthDate;

    @Schema(description = "逝世日期描述")
    private String deathDate;

    @Schema(description = "朝代")
    private String dynasty;

    @Schema(description = "籍贯或出生地")
    private String birthPlace;

    @Schema(description = "人物图片地址")
    private String imageUrl;

    @Schema(description = "人物传记/详细介绍")
    private String biography;

    @Schema(description = "代表作品（逗号分隔）")
    private String works;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
