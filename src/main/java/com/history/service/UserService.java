package com.history.service;

import com.history.model.dto.UpdatePasswordDTO;
import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;

/**
 * @Author Diamond
 * @Create 2026/3/25
 */
public interface UserService {

    /**
     * 获取当前登录用户信息。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    User getCurrentUserInfo(long id);

    /**
     * 修改用户信息。
     *
     * @param id
     * @param updateProfileDTO
     * @return
     */
    User update(long id, UpdateUserProfileDTO updateProfileDTO);

    /**
     * 修改用户密码。
     *
     * @param id                用户 ID
     * @param updatePasswordDTO 包含原密码、新密码、确认新密码
     */
    void changePassword(long id, UpdatePasswordDTO updatePasswordDTO);

    /**
     * 更新用户头像地址。
     *
     * @param userId    用户 ID
     * @param avatarUrl OSS 返回的头像访问 URL
     * @return 更新后的用户信息
     */
    User updateAvatar(long userId, String avatarUrl);
}
