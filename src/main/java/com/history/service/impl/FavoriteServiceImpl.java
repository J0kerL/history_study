package com.history.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.history.common.PageResult;
import com.history.exception.BusinessException;
import com.history.mapper.EventMapper;
import com.history.mapper.FavoriteMapper;
import com.history.mapper.FigureMapper;
import com.history.model.dto.AddFavoriteDTO;
import com.history.model.dto.FavoriteQueryDTO;
import com.history.model.vo.FavoriteVO;
import com.history.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private EventMapper eventMapper;

    @Resource
    private FigureMapper figureMapper;

    /**
     * 分页查询收藏列表
     */
    @Override
    public PageResult<FavoriteVO> listFavorites(Long userId, FavoriteQueryDTO queryDTO) {
        // 1. 开启分页（PageHelper 会自动拦截后续的 SQL 查询）
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 根据用户 ID 查询收藏列表
        List<FavoriteVO> favorites = favoriteMapper.selectFavoritesByUserId(
                userId,
                queryDTO.getType() != null ? queryDTO.getType().byteValue() : null
        );

        // 3. 将 PageHelper 的 PageInfo 转换为统一的 PageResult
        PageInfo<FavoriteVO> pageInfo = new PageInfo<>(favorites);
        return new PageResult<>(pageInfo);
    }

    /**
     * 添加收藏
     */
    @Override
    public void addFavorite(AddFavoriteDTO addFavoriteDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        Integer type = addFavoriteDTO.getType();
        Long refId = addFavoriteDTO.getRefId();

        // 1. 如果收藏类型为事件
        if (type == 1) {
            if (eventMapper.selectById(refId) == null) {
                throw new BusinessException("收藏的资源不存在");
            }
        } else if (type == 2) {
            // 2. 如果收藏类型为人物
            if (figureMapper.selectById(refId) == null) {
                throw new BusinessException("收藏的资源不存在");
            }
        } else {
            // 3. 如果收藏类型既不是事件 也不是人物，则抛出异常
            throw new BusinessException("收藏类型不支持");
        }

        try {
            // 4. 插入收藏记录
            favoriteMapper.insertFavorite(userId, type, refId);
        } catch (DuplicateKeyException e) {
            // 5. 如果收藏记录已存在，则抛出异常
            throw new BusinessException("请勿重复收藏");
        }
    }
}
