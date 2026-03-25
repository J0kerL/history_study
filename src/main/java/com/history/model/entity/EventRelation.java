package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件关联实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "事件关联实体")
public class EventRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 源事件ID。 */
    @Schema(description = "源事件ID")
    private Long eventId;

    /** 关联事件ID。 */
    @Schema(description = "关联事件ID")
    private Long relatedId;

    /** 排序权重。 */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
