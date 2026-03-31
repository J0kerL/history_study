package com.history.mapper;

import com.history.model.entity.Figure;
import org.apache.ibatis.annotations.Param;

public interface FigureMapper {

    /**
     * 根据 ID 查询人物
     */
    Figure selectById(@Param("id") Long id);

}
