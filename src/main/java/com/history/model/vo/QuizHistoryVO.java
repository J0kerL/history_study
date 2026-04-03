package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 历史答题记录 VO。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "历史答题记录VO")
public class QuizHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "答题日期")
    private LocalDate answerDate;

    @Schema(description = "题目内容")
    private String question;

    @Schema(description = "题目类型：1-单选，2-多选")
    private Byte quizType;

    @Schema(description = "正确答案")
    private String correctOptions;

    @Schema(description = "用户选择")
    private String selectedOptions;

    @Schema(description = "是否正确")
    private Boolean correct;
}
