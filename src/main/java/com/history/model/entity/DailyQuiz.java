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
 * 每日题目配置实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日题目配置实体")
public class DailyQuiz implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 题目日期。 */
    @Schema(description = "题目日期")
    private LocalDate quizDate;

    /** 关联题目ID。 */
    @Schema(description = "关联题目ID")
    private Long quizId;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
