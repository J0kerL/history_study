package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * 用户收藏列表查询参数。
 *
 * @author Diamond
 */
@Data
@Schema(description = "用户收藏列表查询参数")
public class FavoriteQueryDTO {

    @Min(value = 1, message = "页码必须大于等于1")
    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "收藏类型筛选（可选）：1=事件，2=人物", example = "1")
    @Min(value = 1, message = "收藏类型仅支持：1=事件，2=人物")
    @Max(value = 2, message = "收藏类型仅支持：1=事件，2=人物")
    private Integer type;
}
