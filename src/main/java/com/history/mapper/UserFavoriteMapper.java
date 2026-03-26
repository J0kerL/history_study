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
public interface UserFavoriteMapper {

    /**
     * 查询用户的收藏列表（分页）。
     *
     * @param userId 用户ID
     * @param type   收藏类型筛选（可选，null 表示不筛选）
     * @return 收藏列表（包含关联对象信息）
     */
    List<FavoriteVO> selectFavoritesByUserId(@Param("userId") Long userId, @Param("type") Byte type);
}
