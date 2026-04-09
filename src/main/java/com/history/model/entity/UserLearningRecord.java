package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户学习记录实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户学习记录实体")
public class UserLearningRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 用户ID。 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 学习日期。 */
    @Schema(description = "学习日期")
    private LocalDate learnDate;

    /** 学习行为类型：1=浏览史今, 2=阅读详情, 3=答题, 4=收藏, 5=搜索。 */
    @Schema(description = "学习行为类型")
    private Byte actionType;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
