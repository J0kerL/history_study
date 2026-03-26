package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

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

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "题目ID")
    private Long quizId;

    @Schema(description = "用户选择的选项")
    private String selectedOptions;

    @Schema(description = "是否正确：0-错误，1-正确")
    private Byte isCorrect;

    @Schema(description = "答题日期")
    private LocalDate answerDate;
}
