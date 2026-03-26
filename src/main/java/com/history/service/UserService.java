package com.history.service;

import com.history.model.dto.UpdateUserProfileDTO;
import com.history.model.entity.User;

/**
 * @Author Diamond
 * @Create 2026/3/25
 */
public interface UserService {
    User getCurrentUserInfo(long id);

    User update(long id, UpdateUserProfileDTO updateProfileDTO);
}
