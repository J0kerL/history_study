package com.history.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.history.common.Result;
import com.history.service.LearningRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习记录 Controller。
 *
 * @author Diamond
 */
@RestController
@RequestMapping("/learning")
@Tag(name = "学习记录", description = "记录用户学习行为，用于统计连续学习天数")
public class LearningRecordController {

    @Resource
    private LearningRecordService learningRecordService;

    @PostMapping("/record")
    @Operation(summary = "记录学习行为", description = "记录用户的学习行为，用于统计连续学习天数")
    public Result<Void> recordLearning(@RequestParam Byte actionType) {
        Long userId = StpUtil.getLoginIdAsLong();
        learningRecordService.recordLearningAction(userId, actionType);
        return Result.success();
    }
}
