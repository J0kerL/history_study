package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 问答题目实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "问答题目实体")
public class Quiz implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
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

    @Schema(description = "正确答案")
    private String correctOptions;

    @Schema(description = "答案解析")
    private String explanation;

    @Schema(description = "难度：1-简单，2-中等，3-困难")
    private Byte difficulty;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "状态：0-下线，1-正常")
    private Byte status;

    @Schema(description = "数据来源：1-人工录入，2-AI生成")
    private Byte source;
}
