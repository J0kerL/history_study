package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "设置收藏状态请求参数")
public class SetFavoriteStatusDTO extends FavoriteDTO {

    @NotNull(message = "收藏状态不能为空")
    @Schema(description = "目标收藏状态：true=收藏，false=取消收藏", example = "true")
    private Boolean favorited;
}
