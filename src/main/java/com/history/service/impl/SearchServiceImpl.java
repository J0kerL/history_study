package com.history.service.impl;

import com.history.mapper.SearchMapper;
import com.history.model.vo.EventSummaryVO;
import com.history.model.vo.FigureSearchVO;
import com.history.model.vo.SearchResultVO;
import com.history.service.LearningRecordService;
import com.history.service.SearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索 Service 实现类。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    private static final int SEARCH_EVENT_LIMIT = 20;
    private static final int SEARCH_FIGURE_LIMIT = 20;
    private static final int HISTORY_LIMIT = 10;

    @Resource
    private SearchMapper searchMapper;

    @Resource
    private LearningRecordService learningRecordService;

    @Override
    public SearchResultVO search(String keyword, Long userId) {
        if (keyword == null || keyword.isBlank()) {
            return new SearchResultVO(Collections.emptyList(), Collections.emptyList());
        }

        String trimmed = keyword.trim();

        // 搜索事件和人物
        List<EventSummaryVO> events = searchMapper.searchEvents(trimmed, SEARCH_EVENT_LIMIT);
        List<FigureSearchVO> figures = searchMapper.searchFigures(trimmed, SEARCH_FIGURE_LIMIT);

        // 记录搜索历史和学习行为（仅登录用户）
        if (userId != null) {
            try {
                // 去重：先删旧记录再插新记录
                searchMapper.deleteByUserAndKeyword(userId, trimmed);
                searchMapper.insertSearchHistory(userId, trimmed);
            } catch (Exception e) {
                log.warn("记录搜索历史失败: userId={}, keyword={}", userId, trimmed, e);
            }

            try {
                learningRecordService.recordLearningAction(userId, (byte) 5);
            } catch (Exception e) {
                log.warn("记录搜索学习行为失败: userId={}", userId, e);
            }
        }

        return new SearchResultVO(events, figures);
    }

    @Override
    public List<String> getHotKeywords() {
        return searchMapper.selectHotKeywords().stream()
                .map(h -> h.getKeyword())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSearchHistory(Long userId) {
        return searchMapper.selectSearchHistory(userId, HISTORY_LIMIT);
    }

    @Override
    public void clearSearchHistory(Long userId) {
        searchMapper.deleteAllByUserId(userId);
    }
}
