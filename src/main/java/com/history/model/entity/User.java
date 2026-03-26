package com.history.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

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

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    /** 序列化时不向前端返回密码哈希，防止敏感信息泄露。 */
    @JsonIgnore
    @Schema(description = "密码")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "注册日期")
    private LocalDate registerDate;

    @Schema(description = "当前连续学习天数")
    private Integer streakDays;

    @Schema(description = "历史最高连续学习天数")
    private Integer maxStreakDays;

    @Schema(description = "累计答题总数")
    private Integer totalQuizCount;

    @Schema(description = "累计答对总数")
    private Integer correctQuizCount;

    @Schema(description = "状态：0-禁用，1-正常")
    private Byte status;
}
