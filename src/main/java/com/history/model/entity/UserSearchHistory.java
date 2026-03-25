package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户搜索历史实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户搜索历史实体")
public class UserSearchHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 用户ID。 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 搜索关键词。 */
    @Schema(description = "搜索关键词")
    private String keyword;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
