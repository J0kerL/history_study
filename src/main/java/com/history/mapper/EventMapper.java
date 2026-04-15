package com.history.mapper;

import com.history.model.entity.Event;
import com.history.model.vo.EventSummaryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventMapper {

    List<EventSummaryVO> selectSummaryByMonthDay(@Param("month") int month, @Param("day") int day);

    Event selectById(@Param("id") Long id);

    List<EventSummaryVO> selectRelatedSummaries(@Param("eventId") Long eventId);

    int insert(Event event);

    /**
     * 批量插入事件，并将数据库生成的自增 ID 回写到各实体的 {@code id} 字段。
     *
     * @param events 待插入的事件列表
     */
    int batchInsert(@Param("events") List<Event> events);

    List<Event> selectRecommendationCandidates(@Param("eventId") Long eventId,
                                               @Param("keywords") List<String> keywords,
                                               @Param("limit") int limit);

    List<Event> selectNearbyRecommendationCandidates(@Param("eventId") Long eventId,
                                                     @Param("year") Short year,
                                                     @Param("limit") int limit);

    int insertRelation(@Param("eventId") Long eventId,
                       @Param("relatedId") Long relatedId,
                       @Param("sortOrder") Integer sortOrder);

    int updateImageUrl(@Param("id") Long id, @Param("imageUrl") String imageUrl);
}
