package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "添加收藏请求参数")
public class AddFavoriteDTO {

    @NotNull(message = "收藏类型不能为空")
    @Min(value = 1, message = "收藏类型仅支持：1=事件，2=人物")
    @Max(value = 2, message = "收藏类型仅支持：1=事件，2=人物")
    @Schema(description = "收藏类型：1=事件，2=人物", example = "1")
    private Integer type;

    @NotNull(message = "收藏资源ID不能为空")
    @Min(value = 1, message = "收藏资源ID必须大于等于1")
    @Schema(description = "被收藏的资源ID", example = "1001")
    private Long refId;
}
