package com.history.service.impl;

import com.history.exception.BusinessException;
import com.history.mapper.UserMapper;
import com.history.model.entity.User;
import com.history.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author Diamond
 * @Create 2026/3/25
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getCurrentUserInfo(long id) {
        if (id <= 0) {
            throw new BusinessException("用户未登录");
        }
        return userMapper.getCurrentUserInfo(id);
    }

}
