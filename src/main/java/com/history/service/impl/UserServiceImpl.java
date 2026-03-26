package com.history.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.history.exception.BusinessException;
import com.history.mapper.UserMapper;
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
}
