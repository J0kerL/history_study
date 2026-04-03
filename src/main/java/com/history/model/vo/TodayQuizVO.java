package com.history.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 每日一题展示 VO（不含答案）。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日一题展示VO")
public class TodayQuizVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "题目内容")
    private String question;

    @Schema(description = "题目类型：1-单选，2-多选")
    private Byte quizType;

    @Schema(description = "选项A")
    private String optionA;

    @Schema(description = "选项B")
    private String optionB;

    @Schema(description = "选项C")
    private String optionC;

    @Schema(description = "选项D")
    private String optionD;

    @Schema(description = "难度：1-简单，2-中等，3-困难")
    private Byte difficulty;

    @Schema(description = "用户是否已答")
    private Boolean answered;

    // ===== 已答时才返回以下字段 =====

    @Schema(description = "正确答案（已答时返回，如 A 或 AB）")
    private String correctOptions;

    @Schema(description = "用户选择的答案（已答时返回）")
    private String selectedOptions;

    @Schema(description = "是否答对（已答时返回）")
    private Boolean correct;

    @Schema(description = "答案解析（已答时返回）")
    private String explanation;
}
