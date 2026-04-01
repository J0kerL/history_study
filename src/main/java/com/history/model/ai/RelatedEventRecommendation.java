package com.history.model.ai;

import lombok.Data;

/**
 * AI 推荐的关联事件条目。
 */
@Data
public class RelatedEventRecommendation {

    private Long relatedEventId;

    private Integer sortOrder;
}
