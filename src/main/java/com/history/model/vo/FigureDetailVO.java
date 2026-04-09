package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 历史人物详情 VO（推荐页使用）。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "历史人物详情VO")
public class FigureDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "人物ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "副标题/身份描述")
    private String subtitle;

    @Schema(description = "生卒年月，如：1037年1月8日 — 1101年8月24日")
    private String timeRange;

    @Schema(description = "出生地")
    private String birthPlace;

    @Schema(description = "朝代")
    private String dynasty;

    @Schema(description = "人物画像URL")
    private String imageUrl;

    @Schema(description = "人物传记/详细介绍（长文本，多段落）")
    private String biography;

    @Schema(description = "代表作品（逗号分隔）")
    private String works;

    @Schema(description = "推荐理由")
    private String recommendReason;
}
