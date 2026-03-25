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
 * 用户实体。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    @Schema(description = "主键ID")
    private Long id;

    /** 用户名。 */
    @Schema(description = "用户名")
    private String username;

    /** 密码。 */
    @Schema(description = "密码")
    private String password;

    /** 手机号。 */
    @Schema(description = "手机号")
    private String phone;

    /** 头像地址。 */
    @Schema(description = "头像地址")
    private String avatar;

    /** 注册日期。 */
    @Schema(description = "注册日期")
    private LocalDate registerDate;

    /** 当前连续学习天数。 */
    @Schema(description = "当前连续学习天数")
    private Integer streakDays;

    /** 历史最高连续学习天数。 */
    @Schema(description = "历史最高连续学习天数")
    private Integer maxStreakDays;

    /** 累计答题总数。 */
    @Schema(description = "累计答题总数")
    private Integer totalQuizCount;

    /** 累计答对总数。 */
    @Schema(description = "累计答对总数")
    private Integer correctQuizCount;

    /** 状态：0-禁用，1-正常。 */
    @Schema(description = "状态：0-禁用，1-正常")
    private Byte status;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
