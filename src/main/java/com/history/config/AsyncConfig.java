package com.history.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * LLM 推理任务线程池（CPU/网络混合型，并发数较小）。
     * 用于 today-events / related-events 的大模型调用和人物生成主流程。
     */
    @Bean("eventAiTaskExecutor")
    public Executor eventAiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("event-ai-");
        // 优雅停机：等待已提交任务执行完毕，最长等待 60 秒后强制退出
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 图片生成专用线程池（IO 密集型）。
     * 图片生成需轮询远程 API（每次最长 2 分钟），与 LLM 推理任务分离，
     * 避免 sleep 阻塞占满 eventAiTaskExecutor 导致 LLM 任务饥饿。
     */
    @Bean("imageTaskExecutor")
    public Executor imageTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("image-gen-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
}
