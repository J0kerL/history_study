package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 每日推荐实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日推荐实体")
public class DailyRecommendation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "推荐日期")
    private LocalDate recDate;

    @Schema(description = "推荐类型：1-事件，2-人物")
    private Byte type;

    @Schema(description = "关联事件或人物ID")
    private Long refId;
}
