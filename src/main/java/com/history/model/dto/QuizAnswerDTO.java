package com.history.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 提交答题请求 DTO。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交答题请求")
public class QuizAnswerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "选择的选项，如 A,AB,CDE")
    private String selectedOptions;
}
