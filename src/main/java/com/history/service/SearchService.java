package com.history.service;

import com.history.model.vo.SearchResultVO;

import java.util.List;

/**
 * 搜索 Service。
 *
 * @author Diamond
 */
public interface SearchService {

    /**
     * 搜索事件和人物。
     *
     * @param keyword 搜索关键词
     * @param userId  当前登录用户ID，未登录传 null
     * @return 搜索结果
     */
    SearchResultVO search(String keyword, Long userId);

    /**
     * 获取热门搜索词。
     */
    List<String> getHotKeywords();

    /**
     * 获取用户搜索历史。
     */
    List<String> getSearchHistory(Long userId);

    /**
     * 清空用户搜索历史。
     */
    void clearSearchHistory(Long userId);
}
