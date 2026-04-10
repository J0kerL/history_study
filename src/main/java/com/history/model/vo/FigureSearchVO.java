package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人物搜索结果 VO。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "人物搜索结果VO")
public class FigureSearchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "人物ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "副标题/身份描述")
    private String subtitle;

    @Schema(description = "朝代")
    private String dynasty;

    @Schema(description = "人物画像URL")
    private String imageUrl;
}
