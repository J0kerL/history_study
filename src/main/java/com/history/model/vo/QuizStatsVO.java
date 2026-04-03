package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户学习统计 VO。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户学习统计VO")
public class QuizStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前连续学习天数")
    private Integer streakDays;

    @Schema(description = "历史最高连续学习天数")
    private Integer maxStreakDays;

    @Schema(description = "累计答题总数")
    private Integer totalQuizCount;

    @Schema(description = "累计答对总数")
    private Integer correctQuizCount;

    @Schema(description = "正确率（百分比，保留1位小数）")
    private Double accuracyRate;
}
