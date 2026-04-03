package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 答题结果 VO。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "答题结果VO")
public class QuizAnswerResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private Long quizId;

    @Schema(description = "正确答案")
    private String correctOptions;

    @Schema(description = "用户选择")
    private String selectedOptions;

    @Schema(description = "是否正确：true-正确，false-错误")
    private Boolean correct;

    @Schema(description = "答案解析")
    private String explanation;
}
