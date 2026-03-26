package com.history.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.history.exception.BusinessException;
import com.history.mapper.AchievementMapper;
import com.history.mapper.UserAchievementMapper;
import com.history.mapper.UserMapper;
import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.Achievement;
import com.history.model.entity.User;
import com.history.model.entity.UserAchievement;
import com.history.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现类。
 *
 * @author Diamond
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserAchievementMapper userAchievementMapper;
    @Resource
    private AchievementMapper achievementMapper;

    @Override
    public User getCurrentUserInfo(long id) {
        return userMapper.selectById(id);
    }

    /**
     * 动态更新用户个人信息。
     *
     * @param id               当前登录用户 ID
     * @param updateProfileDTO 待更新内容（全部字段均可空）
     * @return 更新后的用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(long id, UpdateUserProfileDTO updateProfileDTO) {

        // 1. 判断是否至少填写了一个字段，防止无意义的空更新
        if (StrUtil.isBlank(updateProfileDTO.getUsername())
                && StrUtil.isBlank(updateProfileDTO.getPassword())
                && StrUtil.isBlank(updateProfileDTO.getPhone())) {
            throw new BusinessException("请至少填写一项需要更新的内容");
        }

        // 2. 根据 id 查询用户，确保目标用户存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 用户名唯一性校验：仅当唯一性字段被填写且与当前值不同时才查
        //    这样可以避免用户仅更新其他字段时不必要的查询
        if (StrUtil.isNotBlank(updateProfileDTO.getUsername())
                && !updateProfileDTO.getUsername().equals(user.getUsername())) {
            if (userMapper.selectByUsername(updateProfileDTO.getUsername()) != null) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 4. 手机号唯一性校验：逻辑同上
        if (StrUtil.isNotBlank(updateProfileDTO.getPhone())
                && !updateProfileDTO.getPhone().equals(user.getPhone())) {
            if (userMapper.selectByPhone(updateProfileDTO.getPhone()) != null) {
                throw new BusinessException("手机号已存在");
            }
        }

        // 5. 密码安全处理：若用户修改了密码，需在写入数据库前进行 BCrypt 哈希
        if (StrUtil.isNotBlank(updateProfileDTO.getPassword())) {
            updateProfileDTO.setPassword(BCrypt.hashpw(updateProfileDTO.getPassword()));
        }

        // 6. 执行动态更新，同时捕获数据库层唯一键异常（并发操作导致的唯一性冲突）
        try {
            userMapper.update(id, updateProfileDTO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名或手机号已存在");
        }

        // 7. 返回更新后的用户信息
        return userMapper.selectById(id);
    }

    /**
     * 更新用户头像地址。
     *
     * @param userId    当前登录用户 ID
     * @param avatarUrl OSS 返回的头像访问 URL
     * @return 更新后的用户信息
     */
    @Override
    public User updateAvatar(long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        return userMapper.selectById(userId);
    }

    /**
     * 查询用户成就列表。
     *
     * @param id
     * @return
     */
    @Override
    public List<String> listAchievements(long id) {
        // 1. 根据 id 查询用户，校验用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 2. 根据用户 ID 查询成就列表
        List<UserAchievement> userAchievements = userAchievementMapper.selectByUserId(id);
        if (userAchievements == null || userAchievements.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 批量查询成就定义，避免 N+1 查询
        List<Integer> achievementIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (achievementIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Achievement> achievements = achievementMapper.selectByIds(achievementIds);
        Map<Integer, Achievement> achievementById = achievements.stream()
                .filter(Objects::nonNull)
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Achievement::getId, a -> a, (a1, a2) -> a1));

        // 4. 按 user_achievement 的顺序组装成就名称
        //    (SQL 已按 unlocked_at DESC 排序，所以这里不会破坏顺序)
        List<String> achievementNames = new ArrayList<>(userAchievements.size());
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
            achievementNames.add(achievement.getName());
        }

        return achievementNames;
    }
}
