package com.history.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.history.common.PageResult;
import com.history.mapper.UserFavoriteMapper;
import com.history.model.dto.FavoriteQueryDTO;
import com.history.model.vo.FavoriteVO;
import com.history.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户收藏 Service 实现类。
 *
 * @author Diamond
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private UserFavoriteMapper userFavoriteMapper;

    /**
     * 查询用户的收藏列表（分页）。
     *
     * @param userId   用户ID
     * @param queryDTO 查询参数
     * @return 分页的收藏列表
     */
    @Override
    public PageResult<FavoriteVO> listFavorites(Long userId, FavoriteQueryDTO queryDTO) {
        // 1. 开启分页（PageHelper 会自动拦截后续的 SQL 查询）
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 查询收藏列表（PageHelper 会自动添加 LIMIT 和 OFFSET）
        List<FavoriteVO> favorites = userFavoriteMapper.selectFavoritesByUserId(
                userId,
                queryDTO.getType() != null ? queryDTO.getType().byteValue() : null
        );

        // 3. 将 PageHelper 的 PageInfo 转换为统一的 PageResult
        PageInfo<FavoriteVO> pageInfo = new PageInfo<>(favorites);
        return new PageResult<>(pageInfo);
    }
}
