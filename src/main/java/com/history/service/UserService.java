package com.history.service;

import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/25
 */
public interface UserService {
    User getCurrentUserInfo(long id);

    User update(long id, UpdateUserProfileDTO updateProfileDTO);

    /**
     * 更新用户头像地址。
     *
     * @param userId    用户 ID
     * @param avatarUrl OSS 返回的头像访问 URL
     * @return 更新后的用户信息
     */
    User updateAvatar(long userId, String avatarUrl);

    List<String> listAchievements(long id);
}
