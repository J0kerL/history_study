package com.history.model.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 推荐的关联事件结果。
 */
@Data
public class RelatedEventRecommendationResult {

    private List<RelatedEventRecommendation> recommendations;
}
