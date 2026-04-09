package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 每日推荐响应 VO。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日推荐响应VO")
public class DailyRecommendationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "推荐日期")
    private String recommendDate;

    @Schema(description = "人物详情")
    private FigureDetailVO figure;
}
