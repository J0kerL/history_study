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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        // 1. 根据 id 查询用户，校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 开启分页（PageHelper 会自动对接下来的 SQL）
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 3. 根据用户 ID 查询成就关系（已由 mapper 增加 unlocked_at DESC 排序）
        List<UserAchievement> userAchievements = achievementMapper.selectByUserId(userId);
        if (userAchievements == null) {
            userAchievements = new ArrayList<>();
        }
        PageInfo<UserAchievement> pageInfo = new PageInfo<>(userAchievements);

        if (userAchievements.isEmpty()) {
            return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), new ArrayList<>());
        }

        // 4. 批量查询成就定义，避免 N+1 查询
        List<Integer> achievementIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (achievementIds.isEmpty()) {
            return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), new ArrayList<>());
        }

        List<Achievement> achievements = achievementMapper.selectByIds(achievementIds);
        if (achievements == null) {
            achievements = new ArrayList<>();
        }
        Map<Integer, Achievement> achievementById = achievements.stream()
                .filter(Objects::nonNull)
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Achievement::getId, a -> a, (a1, a2) -> a1));

        // 5. 按 user_achievement 的顺序组装成就定义（SQL 已按 unlocked_at DESC 排序）
        List<Achievement> achievementList = new ArrayList<>(userAchievements.size());
        for (UserAchievement userAchievement : userAchievements) {
            if (userAchievement == null) {
                continue;
            }
            Integer achievementId = userAchievement.getAchievementId();
            if (achievementId == null) {
                throw new BusinessException("成就数据不一致");
            }
            Achievement achievement = achievementById.get(achievementId);
            if (achievement == null || achievement.getName() == null) {
                throw new BusinessException("成就数据不一致");
            }
            achievementList.add(achievement);
        }

        return PageResult.of(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                achievementList
        );
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
                    case 4: // 答题正确率
                        if (user.getTotalQuizCount() != null && user.getTotalQuizCount() > 0
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

