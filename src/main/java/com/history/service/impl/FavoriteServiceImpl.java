package com.history.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.history.common.PageResult;
import com.history.exception.BusinessException;
import com.history.mapper.EventMapper;
import com.history.mapper.FavoriteMapper;
import com.history.mapper.FigureMapper;
import com.history.model.dto.FavoriteDTO;
import com.history.model.dto.FavoriteQueryDTO;
import com.history.model.dto.SetFavoriteStatusDTO;
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
     * 分页查询收藏列表。
     */
    @Override
    public PageResult<FavoriteVO> listFavorites(Long userId, FavoriteQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        List<FavoriteVO> favorites = favoriteMapper.selectFavoritesByUserId(
                userId,
                queryDTO.getType() != null ? queryDTO.getType().byteValue() : null
        );

        PageInfo<FavoriteVO> pageInfo = new PageInfo<>(favorites);
        return new PageResult<>(pageInfo);
    }

    /**
     * 添加收藏。
     */
    @Override
    public void addFavorite(FavoriteDTO favoriteDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        Integer type = favoriteDTO.getType();
        Long refId = favoriteDTO.getRefId();

        validateTargetExists(type, refId);

        try {
            favoriteMapper.insertFavorite(userId, type, refId);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("请勿重复收藏");
        }
    }

    /**
     * 取消收藏。
     */
    @Override
    public void removeFavorite(FavoriteDTO favoriteDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        Integer type = favoriteDTO.getType();
        Long refId = favoriteDTO.getRefId();

        int affectedRows = favoriteMapper.deleteFavorite(userId, type, refId);
        if (affectedRows == 0) {
            throw new BusinessException("收藏记录不存在");
        }
    }

    /**
     * 查询当前用户是否已收藏指定资源。
     */
    @Override
    public boolean hasFavorite(FavoriteDTO favoriteDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        Integer type = favoriteDTO.getType();
        Long refId = favoriteDTO.getRefId();

        return favoriteMapper.countFavorite(userId, type, refId) > 0;
    }

    /**
     * 设置当前用户对指定资源的收藏状态。
     * 幂等语义：
     * 1. 目标状态为已收藏且当前未收藏时，执行收藏。
     * 2. 目标状态为未收藏且当前已收藏时，执行取消收藏。
     * 3. 当前状态与目标状态一致时，不重复写库。
     */
    @Override
    public boolean setFavoriteStatus(SetFavoriteStatusDTO setFavoriteStatusDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        Integer type = setFavoriteStatusDTO.getType();
        Long refId = setFavoriteStatusDTO.getRefId();
        boolean favorited = Boolean.TRUE.equals(setFavoriteStatusDTO.getFavorited());

        boolean currentStatus = favoriteMapper.countFavorite(userId, type, refId) > 0;
        if (currentStatus == favorited) {
            return currentStatus;
        }

        if (favorited) {
            validateTargetExists(type, refId);
            try {
                favoriteMapper.insertFavorite(userId, type, refId);
            } catch (DuplicateKeyException e) {
                return true;
            }
            return true;
        }

        favoriteMapper.deleteFavorite(userId, type, refId);
        return false;
    }

    /**
     * 校验收藏目标是否存在。
     */
    private void validateTargetExists(Integer type, Long refId) {
        if (type == 1) {
            if (eventMapper.selectById(refId) == null) {
                throw new BusinessException("收藏的资源不存在");
            }
            return;
        }

        if (type == 2) {
            if (figureMapper.selectById(refId) == null) {
                throw new BusinessException("收藏的资源不存在");
            }
            return;
        }

        throw new BusinessException("收藏类型不支持");
    }
}
