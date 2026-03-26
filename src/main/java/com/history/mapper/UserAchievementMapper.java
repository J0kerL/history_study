package com.history.mapper;

import com.history.model.entity.UserAchievement;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/3/26
 */
public interface UserAchievementMapper {

    List<UserAchievement> selectByUserId(long id);

}
