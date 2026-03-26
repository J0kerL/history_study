package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户成就列表分页查询参数。
 *
 * @author Diamond
 */
@Data
@Schema(description = "用户成就列表分页查询参数")
public class AchievementQueryDTO {

    @Min(value = 1, message = "页码必须大于等于1")
    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量过大，请限制在 100 以内")
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}

