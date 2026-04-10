package com.history.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.history.common.PageResult;
import com.history.exception.BusinessException;
import com.history.mapper.AchievementMapper;
import com.history.mapper.UserMapper;
import com.history.model.dto.AchievementQueryDTO;
import com.history.model.entity.Achievement;
import com.history.model.entity.User;
import com.history.model.entity.UserAchievement;
import com.history.service.AchievementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户成就 Service 实现类。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private AchievementMapper achievementMapper;

    @Override
    public PageResult<Achievement> listAchievements(Long userId, AchievementQueryDTO queryDTO) {
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询全部成就定义（分页）
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Achievement> allAchievements = achievementMapper.selectAll();
        if (allAchievements == null) {
            allAchievements = new ArrayList<>();
        }
        PageInfo<Achievement> pageInfo = new PageInfo<>(allAchievements);

        // 3. 查询用户已解锁成就的映射 achievementId -> unlockedAt
        List<Map<String, Object>> unlockedMaps = achievementMapper.selectUnlockedMapByUserId(userId);
        Map<Integer, LocalDateTime> unlockedMap = new HashMap<>();
        if (unlockedMaps != null) {
            for (Map<String, Object> row : unlockedMaps) {
                Object idObj = row.get("achievementId");
                Object timeObj = row.get("unlockedAt");
                if (idObj != null && timeObj != null) {
                    Integer achievementId = ((Number) idObj).intValue();
                    LocalDateTime unlockedAt = (LocalDateTime) timeObj;
                    unlockedMap.put(achievementId, unlockedAt);
                }
            }
        }

        // 4. 填充 unlockedAt 字段
        for (Achievement achievement : allAchievements) {
            if (achievement != null && achievement.getId() != null) {
                achievement.setUnlockedAt(unlockedMap.get(achievement.getId()));
            }
        }

        return new PageResult<>(pageInfo);
    }

    @Override
    public void unlockAchievement(Long userId, Integer achievementId) {
        List<UserAchievement> current = achievementMapper.selectByUserId(userId);
        boolean alreadyUnlocked = current != null && current.stream()
                .anyMatch(a -> a.getAchievementId().equals(achievementId));
        if (alreadyUnlocked) {
            return;
        }
        achievementMapper.insertUserAchievement(userId, achievementId);
    }

    @Override
    public int countUnlockedAchievements(Long userId) {
        return achievementMapper.countByUserId(userId);
    }

    @Override
    public void checkAndUnlockAchievements(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        // 检查所有未解锁成就
        List<Achievement> allAchievements = achievementMapper.selectAll();
        List<UserAchievement> unlocked = achievementMapper.selectByUserId(userId);
        List<Integer> unlockedIds = unlocked.stream()
                .map(UserAchievement::getAchievementId)
                .toList();

        if (allAchievements != null) {
            for (Achievement achievement : allAchievements) {
                if (achievement == null || unlockedIds.contains(achievement.getId())) {
                    continue;
                }

                boolean shouldUnlock = false;
                switch (achievement.getConditionType()) {
                    case 1: // 连续学习天数
                        shouldUnlock = user.getStreakDays() != null
                                && user.getStreakDays() >= achievement.getConditionValue();
                        break;
                    case 2: // 累计答题
                        shouldUnlock = user.getTotalQuizCount() != null
                                && user.getTotalQuizCount() >= achievement.getConditionValue();
                        break;
                    case 3: // 累计收藏
                        shouldUnlock = user.getTotalFavoriteCount() != null
                                && user.getTotalFavoriteCount() >= achievement.getConditionValue();
                        break;
                    case 4: // 答题正确率（至少答题10道才参与判定）
                        if (user.getTotalQuizCount() != null && user.getTotalQuizCount() >= 10
                                && user.getCorrectQuizCount() != null) {
                            int accuracy = (int) Math.round((user.getCorrectQuizCount() * 100.0) / user.getTotalQuizCount());
                            shouldUnlock = accuracy >= achievement.getConditionValue();
                        }
                        break;
                    default:
                        break;
                }

                if (shouldUnlock) {
                    unlockAchievement(userId, achievement.getId());
                    log.info("用户解锁成就: userId={}, achievementId={}, name={}",
                            userId, achievement.getId(), achievement.getName());
                }
            }
        }
    }
}
