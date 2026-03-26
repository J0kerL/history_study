package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户收藏列表响应对象。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户收藏列表响应对象")
public class FavoriteVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏记录ID")
    private Long id;

    @Schema(description = "收藏类型：1-事件，2-人物")
    private Byte type;

    @Schema(description = "关联对象ID")
    private Long refId;

    @Schema(description = "关联对象标题（事件）或姓名（人物）")
    private String refTitle;

    @Schema(description = "关联对象图片URL")
    private String refImage;

    @Schema(description = "关联对象摘要（事件）或副标题（人物）")
    private String refSummary;
}
