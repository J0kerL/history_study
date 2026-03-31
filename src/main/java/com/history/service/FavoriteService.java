package com.history.service;

import com.history.common.PageResult;
import com.history.model.dto.FavoriteDTO;
import com.history.model.dto.FavoriteQueryDTO;
import com.history.model.vo.FavoriteVO;

/**
 * 用户收藏 Service。
 *
 * @author Diamond
 */
public interface FavoriteService {

    /**
     * 查询用户的收藏列表（分页）。
     *
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @return 分页收藏列表
     */
    PageResult<FavoriteVO> listFavorites(Long userId, FavoriteQueryDTO queryDTO);

    /**
     * 添加收藏。
     *
     * @param favoriteDTO 添加收藏参数
     */
    void addFavorite(FavoriteDTO favoriteDTO);

    /**
     * 取消收藏。
     *
     * @param favoriteDTO 取消收藏参数
     */
    void removeFavorite(FavoriteDTO favoriteDTO);

    /**
     * 查询当前用户是否已收藏指定资源。
     *
     * @param favoriteDTO 查询参数
     * @return true=已收藏，false=未收藏
     */
    boolean hasFavorite(FavoriteDTO favoriteDTO);
}
