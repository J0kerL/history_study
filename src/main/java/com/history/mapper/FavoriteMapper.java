package com.history.mapper;

import com.history.model.vo.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏 Mapper。
 *
 * @author Diamond
 */
@Mapper
public interface FavoriteMapper {

    /**
     * 查询用户的收藏列表（分页）。
     *
     * @param userId 用户ID
     * @param type 收藏类型筛选
     * @return 收藏列表
     */
    List<FavoriteVO> selectFavoritesByUserId(@Param("userId") Long userId, @Param("type") Byte type);

    /**
     * 添加收藏。
     *
     * @param userId 用户ID
     * @param type 收藏类型
     * @param refId 资源ID
     */
    void insertFavorite(@Param("userId") long userId, @Param("type") Integer type, @Param("refId") Long refId);

    /**
     * 取消收藏。
     *
     * @param userId 用户ID
     * @param type 收藏类型
     * @param refId 资源ID
     * @return 受影响行数
     */
    int deleteFavorite(@Param("userId") long userId, @Param("type") Integer type, @Param("refId") Long refId);

    /**
     * 查询是否已收藏。
     *
     * @param userId 用户ID
     * @param type 收藏类型
     * @param refId 资源ID
     * @return 收藏数量
     */
    int countFavorite(@Param("userId") long userId, @Param("type") Integer type, @Param("refId") Long refId);

    /**
     * 统计用户收藏总数。
     *
     * @param userId 用户ID
     * @return 收藏总数
     */
    int countByUserId(@Param("userId") long userId);
}
