package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户收藏实体")
public class UserFavorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 用户ID。 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 收藏类型：1-事件，2-人物。 */
    @Schema(description = "收藏类型：1-事件，2-人物")
    private Byte type;

    /** 关联事件或人物ID。 */
    @Schema(description = "关联事件或人物ID")
    private Long refId;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
