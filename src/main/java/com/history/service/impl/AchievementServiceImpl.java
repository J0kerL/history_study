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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户成就 Service 实现类。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    /** Redis 中缓存全量成就定义的 key，TTL 1 小时（成就列表极少变更）。 */
    private static final String ACHIEVEMENT_ALL_CACHE_KEY = "achievement:all";
    private static final Duration ACHIEVEMENT_CACHE_TTL = Duration.ofHours(1);

    @Resource
    private UserMapper userMapper;

    @Resource
    private AchievementMapper achievementMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
        // INSERT IGNORE 保证幂等，无需应用层先查再插（避免额外的 selectByUserId 和并发竞态）
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

        // 从 Redis 获取缓存的成就定义列表，缓存未命中时回源 DB 并写缓存
        List<Achievement> allAchievements = getCachedAllAchievements();
        if (allAchievements == null || allAchievements.isEmpty()) {
            return;
        }

        // 一次查询获取用户所有已解锁成就 ID，组成 Set 供 O(1) 查找（避免 unlockAchievement 内部重复查询）
        List<UserAchievement> unlocked = achievementMapper.selectByUserId(userId);
        Set<Integer> unlockedIds = unlocked.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        for (Achievement achievement : allAchievements) {
            if (achievement == null || unlockedIds.contains(achievement.getId())) {
                continue;
            }

            boolean shouldUnlock = switch (achievement.getConditionType()) {
                case 1 -> // 连续学习天数
                        user.getStreakDays() != null
                                && user.getStreakDays() >= achievement.getConditionValue();
                case 2 -> // 累计答题
                        user.getTotalQuizCount() != null
                                && user.getTotalQuizCount() >= achievement.getConditionValue();
                case 3 -> // 累计收藏
                        user.getTotalFavoriteCount() != null
                                && user.getTotalFavoriteCount() >= achievement.getConditionValue();
                case 4 -> { // 答题正确率（至少 10 道题才参与判定）
                    if (user.getTotalQuizCount() != null && user.getTotalQuizCount() >= 10
                            && user.getCorrectQuizCount() != null) {
                        int accuracy = (int) Math.round(
                                (user.getCorrectQuizCount() * 100.0) / user.getTotalQuizCount());
                        yield accuracy >= achievement.getConditionValue();
                    }
                    yield false;
                }
                default -> false;
            };

            if (shouldUnlock) {
                // INSERT IGNORE 幂等，无需重复查询
                achievementMapper.insertUserAchievement(userId, achievement.getId());
                log.info("用户解锁成就: userId={}, achievementId={}, name={}",
                        userId, achievement.getId(), achievement.getName());
            }
        }
    }

    /**
     * 从 Redis 获取全量成就定义，未命中时回源数据库并写入缓存。
     */
    @SuppressWarnings("unchecked")
    private List<Achievement> getCachedAllAchievements() {
        Object cached = redisTemplate.opsForValue().get(ACHIEVEMENT_ALL_CACHE_KEY);
        if (cached instanceof List<?> list) {
            return (List<Achievement>) list;
        }
        List<Achievement> achievements = achievementMapper.selectAll();
        if (achievements != null && !achievements.isEmpty()) {
            redisTemplate.opsForValue().set(ACHIEVEMENT_ALL_CACHE_KEY, achievements, ACHIEVEMENT_CACHE_TTL);
        }
        return achievements;
    }
}
