package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成就定义实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成就定义实体")
public class Achievement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Integer id;

    /** 成就编码。 */
    @Schema(description = "成就编码")
    private String code;

    /** 成就名称。 */
    @Schema(description = "成就名称")
    private String name;

    /** 成就描述。 */
    @Schema(description = "成就描述")
    private String description;

    /** 成就图标地址。 */
    @Schema(description = "成就图标地址")
    private String iconUrl;

    /** 解锁条件类型。 */
    @Schema(description = "解锁条件类型")
    private Byte conditionType;

    /** 解锁条件值。 */
    @Schema(description = "解锁条件值")
    private Integer conditionValue;

    /** 解锁时间（仅已解锁时有值）。 */
    @Schema(description = "解锁时间，未解锁时为null")
    private LocalDateTime unlockedAt;
}
