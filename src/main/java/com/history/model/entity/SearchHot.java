package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索热词实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索热词实体")
public class SearchHot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Integer id;

    /** 热门关键词。 */
    @Schema(description = "热门关键词")
    private String keyword;

    /** 排序权重。 */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /** 状态：0-下线，1-展示。 */
    @Schema(description = "状态：0-下线，1-展示")
    private Byte status;

    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
