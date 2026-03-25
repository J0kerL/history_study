package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 题目内容。 */
    @Schema(description = "题目内容")
    private String question;

    /** 题目类型：1-单选，2-多选。 */
    @Schema(description = "题目类型：1-单选，2-多选")
    private Byte quizType;

    /** 选项A。 */
    @Schema(description = "选项A")
    private String optionA;

    /** 选项B。 */
    @Schema(description = "选项B")
    private String optionB;

    /** 选项C。 */
    @Schema(description = "选项C")
    private String optionC;

    /** 选项D。 */
    @Schema(description = "选项D")
    private String optionD;

    /** 正确答案。 */
    @Schema(description = "正确答案")
    private String correctOptions;

    /** 答案解析。 */
    @Schema(description = "答案解析")
    private String explanation;

    /** 难度：1-简单，2-中等，3-困难。 */
    @Schema(description = "难度：1-简单，2-中等，3-困难")
    private Byte difficulty;

    /** 标签。 */
    @Schema(description = "标签")
    private String tags;

    /** 状态：0-下线，1-正常。 */
    @Schema(description = "状态：0-下线，1-正常")
    private Byte status;

    /** 数据来源：1-人工录入，2-大模型生成。 */
    @Schema(description = "数据来源：1-人工录入，2-大模型生成")
    private Byte source;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
