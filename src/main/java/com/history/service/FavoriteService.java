package com.history.service;

import com.history.common.PageResult;
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
     * @param userId  用户ID
     * @param queryDTO 查询参数（页码、每页数量、类型筛选）
     * @return 分页的收藏列表
     */
    PageResult<FavoriteVO> listFavorites(Long userId, FavoriteQueryDTO queryDTO);
}
