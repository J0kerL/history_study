package com.history.service.impl;

import com.history.mapper.LearningRecordMapper;
import com.history.mapper.UserMapper;
import com.history.model.entity.User;
import com.history.service.AchievementService;
import com.history.service.LearningRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 用户学习记录 Service 实现类。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class LearningRecordServiceImpl implements LearningRecordService {

    @Resource
    private LearningRecordMapper learningRecordMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AchievementService achievementService;

    @Override
    public void recordLearningAction(Long userId, Byte actionType) {
        LocalDate today = LocalDate.now();

        // 幂等写入（同一天同一行为只记录一次）
        int rows = learningRecordMapper.insertRecord(userId, today, actionType);
        boolean isNewRecord = rows > 0;

        if (isNewRecord) {
            log.info("记录学习行为: userId={}, actionType={}, date={}", userId, actionType, today);
        }

        // 重新计算连续学习天数（仅新记录时需要）
        if (isNewRecord) {
            int streakDays = calculateStreakDays(userId);

            // 更新用户表的连续天数
            User user = userMapper.selectById(userId);
            if (user != null) {
                Integer oldStreak = user.getStreakDays();
                if (oldStreak == null || !oldStreak.equals(streakDays)) {
                    userMapper.updateStreakDays(userId, streakDays);
                    log.info("更新连续学习天数: userId={}, oldStreak={}, newStreak={}",
                            userId, oldStreak, streakDays);
                }
            }
        }

        // 每次都检查是否解锁成就（同一天重复行为可能导致其他统计变化，如累计收藏数增加）
        achievementService.checkAndUnlockAchievements(userId);
    }

    @Override
    public int calculateStreakDays(Long userId) {
        // 从昨天开始往前数（因为今天可能还没学习）
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        int streak = 0;
        LocalDate checkDate = yesterday;

        // 最多检查 365 天（防止死循环）
        while (streak < 365) {
            boolean hasLearned = learningRecordMapper.hasLearningRecord(userId, checkDate);
            if (!hasLearned) {
                break;
            }
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        // 如果今天已经学习了，+1
        boolean hasTodayRecord = learningRecordMapper.hasLearningRecord(userId, today);
        if (hasTodayRecord) {
            streak++;
        }

        return streak;
    }

    @Override
    public boolean hasLearningRecord(Long userId, LocalDate learnDate) {
        return learningRecordMapper.hasLearningRecord(userId, learnDate);
    }

    @Override
    public LocalDate getLatestLearnDate(Long userId) {
        return learningRecordMapper.getLatestLearnDate(userId);
    }
}
