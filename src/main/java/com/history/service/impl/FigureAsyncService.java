package com.history.service.impl;

import com.history.service.FigureRecommendationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 历史人物异步任务服务。
 * 使用 Spring 托管线程池执行耗时的人物生成任务，替代裸 {@code new Thread()} 调用。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class FigureAsyncService {

    @Resource
    private FigureRecommendationService figureRecommendationService;

    /**
     * 异步批量生成历史人物，用于补充库存。
     * 使用与事件 AI 生成相同的线程池，受线程池容量和队列保护，不会无限制创建线程。
     *
     * @param count 需要生成的人物数量
     */
    @Async("eventAiTaskExecutor")
    public void replenishFiguresAsync(int count) {
        try {
            log.info("异步补充人物库存开始: count={}", count);
            int generated = figureRecommendationService.generateFiguresBatch(count);
            log.info("异步补充人物库存完成: requested={}, generated={}", count, generated);
        } catch (Exception e) {
            log.error("异步补充人物库存失败: count={}", count, e);
        }
    }
}
