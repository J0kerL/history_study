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
 * 用户答题记录实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户答题记录实体")
public class UserQuizRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 用户ID。 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 题目ID。 */
    @Schema(description = "题目ID")
    private Long quizId;

    /** 用户选择的选项。 */
    @Schema(description = "用户选择的选项")
    private String selectedOptions;

    /** 是否答对：0-错误，1-正确。 */
    @Schema(description = "是否答对：0-错误，1-正确")
    private Byte isCorrect;

    /** 答题日期。 */
    @Schema(description = "答题日期")
    private LocalDate answerDate;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
