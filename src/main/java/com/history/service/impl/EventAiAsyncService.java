package com.history.service.impl;

import cn.hutool.core.util.StrUtil;
import com.history.exception.BusinessException;
import com.history.llm.LlmClient;
import com.history.mapper.EventMapper;
import com.history.model.ai.GeneratedTodayEvent;
import com.history.model.ai.RelatedEventRecommendation;
import com.history.model.ai.RelatedEventRecommendationResult;
import com.history.model.ai.TodayEventsGenerationResult;
import com.history.model.entity.Event;
import com.history.model.vo.EventSummaryVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventAiAsyncService {

    private static final byte AI_SOURCE = 2;
    private static final int TODAY_EVENT_LIMIT = 5;
    private static final int RELATED_EVENT_LIMIT = 5;
    private static final int RELATED_CANDIDATE_LIMIT = 20;
    private static final int RELATED_CANDIDATE_FALLBACK_LIMIT = 10;
    private static final int SUMMARY_MAX_LENGTH = 40;
    private static final int CONTENT_MIN_LENGTH = 180;
    private static final short CHINESE_HISTORY_START_YEAR = -2070;
    private static final short CHINESE_HISTORY_END_YEAR = 1911;

    private final Set<String> todayGenerationKeys = ConcurrentHashMap.newKeySet();
    private final Set<Long> relatedGenerationKeys = ConcurrentHashMap.newKeySet();

    @Resource
    private EventMapper eventMapper;

    @Resource
    private LlmClient llmClient;

    @Resource
    private EventSseService eventSseService;

    @Async("eventAiTaskExecutor")
    public void generateTodayEventsAsync(int month, int day) {
        String key = month + "-" + day;
        if (!todayGenerationKeys.add(key)) {
            log.info("今日事件生成任务已在进行中: key={}", key);
            return;
        }

        try {
            if (doGenerateTodayEvents(month, day)) {
                eventSseService.publishTodayReady(month, day);
            } else {
                eventSseService.publishTodayFailed(month, day);
            }
        } catch (Exception e) {
            eventSseService.publishTodayFailed(month, day);
            log.error("今日事件异步生成失败: month={}, day={}", month, day, e);
        } finally {
            todayGenerationKeys.remove(key);
        }
    }

    @Async("eventAiTaskExecutor")
    public void generateRelatedEventsAsync(Event currentEvent) {
        Long eventId = currentEvent.getId();
        if (eventId == null || !relatedGenerationKeys.add(eventId)) {
            log.info("关联事件生成任务已在进行中: eventId={}", eventId);
            return;
        }

        try {
            if (doGenerateRelatedEvents(currentEvent)) {
                eventSseService.publishRelatedReady(eventId);
            } else {
                eventSseService.publishRelatedFailed(eventId);
            }
        } catch (Exception e) {
            eventSseService.publishRelatedFailed(eventId);
            log.error("关联事件异步生成失败: eventId={}", eventId, e);
        } finally {
            relatedGenerationKeys.remove(eventId);
        }
    }

    public boolean isTodayGenerationRunning(int month, int day) {
        return todayGenerationKeys.contains(month + "-" + day);
    }

    public boolean isRelatedGenerationRunning(Long eventId) {
        return eventId != null && relatedGenerationKeys.contains(eventId);
    }

    private boolean doGenerateTodayEvents(int month, int day) {
        Set<String> existingKeys = eventMapper.selectSummaryByMonthDay(month, day).stream()
                .map(this::buildSummaryKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (existingKeys.size() >= TODAY_EVENT_LIMIT) {
            log.info("今日事件已满足数量要求，跳过生成: month={}, day={}, count={}", month, day, existingKeys.size());
            return true;
        }

        TodayEventsGenerationResult result = llmClient.call(
                buildTodayEventsSystemPrompt(month, day),
                buildTodayEventsUserPrompt(month, day),
                TodayEventsGenerationResult.class
        );

        List<GeneratedTodayEvent> generatedEvents = result == null ? null : result.getEvents();
        log.info("大模型今日事件原始结果条数: month={}, day={}, count={}",
                month, day, generatedEvents == null ? 0 : generatedEvents.size());
        if (generatedEvents == null || generatedEvents.isEmpty()) {
            throw new BusinessException("今日事件生成失败，请稍后再试");
        }

        Map<String, Event> validEvents = new LinkedHashMap<>();
        for (GeneratedTodayEvent generatedEvent : generatedEvents) {
            Event event = convertGeneratedTodayEvent(generatedEvent, month, day);
            if (event == null) {
                continue;
            }

            String eventKey = buildEventKey(event);
            if (existingKeys.contains(eventKey)) {
                continue;
            }
            validEvents.putIfAbsent(eventKey, event);
            if (existingKeys.size() + validEvents.size() >= TODAY_EVENT_LIMIT) {
                break;
            }
        }

        if (validEvents.isEmpty()) {
            log.warn("今日事件生成结果无有效新增数据: month={}, day={}", month, day);
            return false;
        }

        for (Event event : validEvents.values()) {
            eventMapper.insert(event);
        }
        log.info("今日事件落库完成: month={}, day={}, savedCount={}", month, day, validEvents.size());
        return true;
    }

    private boolean doGenerateRelatedEvents(Event currentEvent) {
        List<EventSummaryVO> existingRelations = eventMapper.selectRelatedSummaries(currentEvent.getId());
        if (existingRelations != null && !existingRelations.isEmpty()) {
            log.info("事件已存在关联数据，跳过生成: eventId={}, count={}", currentEvent.getId(), existingRelations.size());
            return true;
        }

        List<String> keywords = extractKeywords(currentEvent);
        List<Event> candidates = loadRecommendationCandidates(currentEvent, keywords);
        log.info("关联事件候选召回完成: eventId={}, keywordCount={}, candidateCount={}",
                currentEvent.getId(), keywords.size(), candidates.size());
        if (candidates.isEmpty()) {
            return false;
        }

        RelatedEventRecommendationResult result = null;
        try {
            result = llmClient.call(
                    buildRelatedEventsSystemPrompt(),
                    buildRelatedEventsUserPrompt(currentEvent, candidates),
                    RelatedEventRecommendationResult.class
            );
        } catch (BusinessException e) {
            log.warn("关联事件大模型推荐失败，使用候选兜底方案: eventId={}", currentEvent.getId(), e);
        }

        List<RelatedEventRecommendation> recommendations = result == null ? null : result.getRecommendations();
        log.info("大模型关联推荐原始结果条数: eventId={}, count={}",
                currentEvent.getId(), recommendations == null ? 0 : recommendations.size());

        Set<Long> savedIds = saveRecommendedRelations(currentEvent, candidates, recommendations);
        if (savedIds.isEmpty()) {
            savedIds = saveFallbackRelations(currentEvent, candidates);
        }
        log.info("关联事件落库完成: eventId={}, savedCount={}", currentEvent.getId(), savedIds.size());
        return !savedIds.isEmpty();
    }

    private Set<Long> saveRecommendedRelations(Event currentEvent,
                                               List<Event> candidates,
                                               List<RelatedEventRecommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> candidateIds = candidates.stream()
                .map(Event::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> savedIds = new LinkedHashSet<>();
        int fallbackSortOrder = RELATED_EVENT_LIMIT * 10;
        for (RelatedEventRecommendation recommendation : recommendations) {
            if (recommendation == null || recommendation.getRelatedEventId() == null) {
                continue;
            }

            Long relatedEventId = recommendation.getRelatedEventId();
            if (relatedEventId.equals(currentEvent.getId())
                    || !candidateIds.contains(relatedEventId)
                    || !savedIds.add(relatedEventId)) {
                continue;
            }

            Integer sortOrder = recommendation.getSortOrder();
            if (sortOrder == null) {
                sortOrder = fallbackSortOrder;
            }
            fallbackSortOrder -= 10;

            eventMapper.insertRelation(currentEvent.getId(), relatedEventId, sortOrder);
            if (savedIds.size() >= RELATED_EVENT_LIMIT) {
                break;
            }
        }
        return savedIds;
    }

    private Set<Long> saveFallbackRelations(Event currentEvent, List<Event> candidates) {
        Set<Long> savedIds = new LinkedHashSet<>();
        int sortOrder = RELATED_EVENT_LIMIT * 10;
        for (Event candidate : candidates) {
            if (candidate == null || candidate.getId() == null || candidate.getId().equals(currentEvent.getId())) {
                continue;
            }
            if (!savedIds.add(candidate.getId())) {
                continue;
            }
            eventMapper.insertRelation(currentEvent.getId(), candidate.getId(), sortOrder);
            sortOrder -= 10;
            if (savedIds.size() >= RELATED_EVENT_LIMIT) {
                break;
            }
        }
        log.info("关联事件使用候选兜底落库: eventId={}, savedCount={}", currentEvent.getId(), savedIds.size());
        return savedIds;
    }

    private List<Event> loadRecommendationCandidates(Event currentEvent, List<String> keywords) {
        LinkedHashMap<Long, Event> candidates = new LinkedHashMap<>();

        mergeCandidates(candidates, eventMapper.selectRecommendationCandidates(
                currentEvent.getId(),
                keywords,
                RELATED_CANDIDATE_LIMIT
        ));

        if (candidates.size() < RELATED_EVENT_LIMIT) {
            mergeCandidates(candidates, eventMapper.selectNearbyRecommendationCandidates(
                    currentEvent.getId(),
                    currentEvent.getYear(),
                    RELATED_CANDIDATE_FALLBACK_LIMIT
            ));
        }

        return new ArrayList<>(candidates.values());
    }

    private void mergeCandidates(Map<Long, Event> target, List<Event> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        for (Event candidate : candidates) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            target.putIfAbsent(candidate.getId(), candidate);
            if (target.size() >= RELATED_CANDIDATE_LIMIT) {
                break;
            }
        }
    }

    private Event convertGeneratedTodayEvent(GeneratedTodayEvent generatedEvent, int month, int day) {
        if (generatedEvent == null
                || StrUtil.isBlank(generatedEvent.getTitle())
                || generatedEvent.getYear() == null
                || StrUtil.isBlank(generatedEvent.getSummary())
                || StrUtil.isBlank(generatedEvent.getContent())
                || StrUtil.isBlank(generatedEvent.getTags())) {
            return null;
        }

        String title = StrUtil.trim(generatedEvent.getTitle());
        String summary = StrUtil.trim(generatedEvent.getSummary());
        String content = StrUtil.trim(generatedEvent.getContent());
        String tags = normalizeTags(generatedEvent.getTags());

        if (StrUtil.isBlank(title)
                || StrUtil.isBlank(summary)
                || StrUtil.isBlank(content)
                || StrUtil.isBlank(tags)
                || !isWithinChineseHistoryRange(generatedEvent.getYear())
                || content.length() < CONTENT_MIN_LENGTH) {
            return null;
        }

        Event event = new Event();
        event.setTitle(title);
        event.setYear(generatedEvent.getYear().shortValue());
        event.setMonth((byte) month);
        event.setDay((byte) day);
        event.setSummary(StrUtil.maxLength(summary, SUMMARY_MAX_LENGTH));
        event.setContent(content);
        event.setTags(tags);
        event.setSource(AI_SOURCE);
        return event;
    }

    private List<String> extractKeywords(Event event) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        if (StrUtil.isNotBlank(event.getTags())) {
            String[] parts = event.getTags().split("[,，]");
            for (String part : parts) {
                String keyword = StrUtil.trim(part);
                if (StrUtil.isNotBlank(keyword)) {
                    keywords.add(keyword);
                }
            }
        }
        if (StrUtil.isNotBlank(event.getTitle())) {
            keywords.add(StrUtil.trim(event.getTitle()));
        }
        return new ArrayList<>(keywords);
    }

    private String normalizeTags(String tags) {
        return StrUtil.trim(tags)
                .replace('，', ',')
                .replaceAll("\\s+", "");
    }

    private String buildTodayEventsSystemPrompt(int month, int day) {
        return "你是中国历史学习应用的数据生成助手。"
                + "你只能生成中华历史事件，时间范围限定为夏商周至清末，年份必须在 "
                + CHINESE_HISTORY_START_YEAR + " 到 " + CHINESE_HISTORY_END_YEAR + " 之间。"
                + "禁止生成中国近现代史、当代史、世界史或外国历史事件。"
                + "请使用客观、中性、百科式表述，不要渲染冲突细节。"
                + "必须返回严格 JSON，字段结构为 {\"events\":[{\"title\":\"\",\"year\":0,\"summary\":\"\",\"content\":\"\",\"tags\":\"标签1,标签2\"}]}。"
                + "一次生成 5 条，不要输出 Markdown，不要输出解释。"
                + "summary 必须是 25 到 40 字的中文摘要。"
                + "content 必须是可直接展示的详细中文正文，长度至少 180 字，建议 3 到 5 句，说明背景、经过和历史影响。"
                + "同一天的 5 条事件必须彼此不同，标题不能重复，尽量覆盖不同朝代。"
                + "所有字段值里都不要出现 ASCII 英文双引号，若需要引用，请改用中文引号或直接改写。"
                + "tags 使用英文逗号分隔，并体现中国历史时期，如夏朝、商朝、西周、东周、春秋、战国、秦朝、汉朝、三国、晋朝、南北朝、隋朝、唐朝、宋朝、元朝、明朝、清朝、晚清等。";
    }

    private String buildTodayEventsUserPrompt(int month, int day) {
        return "请生成 5 条发生在 " + month + " 月 " + day + " 日的中华历史事件。"
                + "要求只使用夏商周到清末的中国历史事件，彼此不重复，尽量覆盖不同朝代。"
                + "正文要足够详细，让用户点进详情后能够直接了解事件背景、经过和影响。";
    }

    private String buildRelatedEventsSystemPrompt() {
        return "你是中国历史学习应用的关联推荐助手。"
                + "你只能从给定候选事件中选择最相关的事件，不能编造新的事件 ID。"
                + "优先选择与当前事件在朝代、历史阶段、人物、战争、制度变迁或因果影响上最相关的候选事件。"
                + "必须返回严格 JSON，字段结构为 {\"recommendations\":[{\"relatedEventId\":0,\"sortOrder\":100}]}。"
                + "一次返回 3 到 5 条，sortOrder 越大表示越靠前。"
                + "所有字段值里都不要出现 ASCII 英文双引号。"
                + "不要输出 Markdown，不要输出解释。";
    }

    private String buildRelatedEventsUserPrompt(Event currentEvent, List<Event> candidates) {
        String candidateText = candidates.stream()
                .sorted(Comparator.comparing(Event::getId))
                .map(candidate -> "ID=" + candidate.getId()
                        + "；标题=" + candidate.getTitle()
                        + "；年份=" + candidate.getYear()
                        + "；摘要=" + candidate.getSummary()
                        + "；标签=" + candidate.getTags())
                .collect(Collectors.joining("\n"));

        return "当前事件："
                + "标题=" + currentEvent.getTitle()
                + "；年份=" + currentEvent.getYear()
                + "；摘要=" + currentEvent.getSummary()
                + "；标签=" + currentEvent.getTags()
                + "\n候选事件如下，请只从候选中选择最相关的 3 到 5 条：\n"
                + candidateText;
    }

    private String buildSummaryKey(EventSummaryVO event) {
        return event.getYear() + "::" + normalizeTitle(event.getTitle());
    }

    private String buildEventKey(Event event) {
        return event.getYear() + "::" + normalizeTitle(event.getTitle());
    }

    private String normalizeTitle(String title) {
        return StrUtil.trim(title).replaceAll("\\s+", "");
    }

    private boolean isWithinChineseHistoryRange(Integer year) {
        if (year == null) {
            return false;
        }
        return year >= CHINESE_HISTORY_START_YEAR && year <= CHINESE_HISTORY_END_YEAR;
    }
}
