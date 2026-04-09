package com.history.service;

import com.history.model.vo.DailyRecommendationVO;

/**
 * 每日推荐 Service。
 *
 * @author Diamond
 */
public interface FigureRecommendationService {

    /**
     * 获取今日推荐人物（如不存在则自动生成）。
     */
    DailyRecommendationVO getTodayRecommendation();

    /**
     * 批量生成历史人物数据到数据库。
     *
     * @param count 生成数量
     * @return 实际生成数量
     */
    int generateFiguresBatch(int count);
}
