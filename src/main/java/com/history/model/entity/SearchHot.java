package com.history.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 搜索热词实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索热词实体")
public class SearchHot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "热门关键词")
    private String keyword;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "状态：0-下线，1-展示")
    private Byte status;
}
