package com.history.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.history.exception.BusinessException;
import com.history.mapper.UserMapper;
import com.history.model.dto.UpdatePasswordDTO;
import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;
import com.history.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类。
 *
 * @author Diamond
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getCurrentUserInfo(long id) {
        return userMapper.selectById(id);
    }

    /**
     * 动态更新用户个人信息（用户名、手机号）。
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
                && StrUtil.isBlank(updateProfileDTO.getPhone())) {
            throw new BusinessException("请至少填写一项需要更新的内容");
        }

        // 2. 根据 id 查询用户，确保目标用户存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 用户名唯一性校验：仅当唯一性字段被填写且与当前值不同时才查
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

        // 5. 执行动态更新，同时捕获数据库层唯一键异常（并发操作导致的唯一性冲突）
        try {
            userMapper.update(id, updateProfileDTO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名或手机号已存在");
        }

        // 6. 返回更新后的用户信息
        return userMapper.selectById(id);
    }

    /**
     * 修改用户密码。
     * 校验原密码正确性，以及新密码与确认密码一致性，然后 BCrypt 加密后更新。
     *
     * @param id                当前登录用户 ID
     * @param updatePasswordDTO 原密码、新密码、确认新密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(long id, UpdatePasswordDTO updatePasswordDTO) {

        // 1. 查询用户，确保存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 校验原密码是否正确
        if (!BCrypt.checkpw(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 3. 校验新密码与确认密码是否一致
        if (!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getConfirmNewPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        // 4. 新密码不能与原密码相同
        if (BCrypt.checkpw(updatePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        // 5. BCrypt 加密新密码并更新
        String encodedPassword = BCrypt.hashpw(updatePasswordDTO.getNewPassword());
        userMapper.updatePassword(id, encodedPassword);
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
}
