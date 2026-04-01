package com.history.service.impl;

import com.history.model.vo.EventGenerationNotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class EventSseService {

    private static final long SSE_TIMEOUT_MILLIS = 10 * 60 * 1000L;

    private final Map<String, List<SseEmitter>> todayEmitters = new ConcurrentHashMap<>();
    private final Map<Long, List<SseEmitter>> relatedEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeTodayEvents(int month, int day) {
        String key = buildTodayKey(month, day);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        registerEmitter(todayEmitters, key, emitter);
        sendEvent(emitter, "subscribed", new EventGenerationNotificationVO(
                "today-events",
                "subscribed",
                "今日事件订阅已建立",
                month,
                day,
                null
        ));
        return emitter;
    }

    public SseEmitter subscribeRelatedEvents(Long eventId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        registerEmitter(relatedEmitters, eventId, emitter);
        sendEvent(emitter, "subscribed", new EventGenerationNotificationVO(
                "related-events",
                "subscribed",
                "关联事件订阅已建立",
                null,
                null,
                eventId
        ));
        return emitter;
    }

    public void publishTodayReady(int month, int day) {
        broadcast(todayEmitters, buildTodayKey(month, day), "today-events-ready", new EventGenerationNotificationVO(
                "today-events",
                "ready",
                "今日事件已生成完成",
                month,
                day,
                null
        ), true);
    }

    public void publishTodayFailed(int month, int day) {
        broadcast(todayEmitters, buildTodayKey(month, day), "today-events-failed", new EventGenerationNotificationVO(
                "today-events",
                "failed",
                "今日事件生成失败",
                month,
                day,
                null
        ), true);
    }

    public void publishRelatedReady(Long eventId) {
        broadcast(relatedEmitters, eventId, "related-events-ready", new EventGenerationNotificationVO(
                "related-events",
                "ready",
                "关联事件已生成完成",
                null,
                null,
                eventId
        ), true);
    }

    public void publishRelatedFailed(Long eventId) {
        broadcast(relatedEmitters, eventId, "related-events-failed", new EventGenerationNotificationVO(
                "related-events",
                "failed",
                "关联事件生成失败",
                null,
                null,
                eventId
        ), true);
    }

    private <K> void registerEmitter(Map<K, List<SseEmitter>> emitterMap, K key, SseEmitter emitter) {
        emitterMap.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(emitterMap, key, emitter));
        emitter.onTimeout(() -> removeEmitter(emitterMap, key, emitter));
        emitter.onError(ex -> removeEmitter(emitterMap, key, emitter));
    }

    private <K> void broadcast(Map<K, List<SseEmitter>> emitterMap,
                               K key,
                               String eventName,
                               EventGenerationNotificationVO payload) {
        broadcast(emitterMap, key, eventName, payload, false);
    }

    private <K> void broadcast(Map<K, List<SseEmitter>> emitterMap,
                               K key,
                               String eventName,
                               EventGenerationNotificationVO payload,
                               boolean completeAfterSend) {
        List<SseEmitter> emitters = emitterMap.get(key);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                sendEvent(emitter, eventName, payload);
                if (completeAfterSend) {
                    emitter.complete();
                }
            } catch (Exception e) {
                removeEmitter(emitterMap, key, emitter);
                log.warn("SSE 推送失败，已移除订阅: eventName={}, key={}", eventName, key, e);
            }
        }
    }

    private void sendEvent(SseEmitter emitter, String eventName, EventGenerationNotificationVO payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload));
        } catch (IOException e) {
            throw new IllegalStateException("failed to send sse event", e);
        }
    }

    private <K> void removeEmitter(Map<K, List<SseEmitter>> emitterMap, K key, SseEmitter emitter) {
        List<SseEmitter> emitters = emitterMap.get(key);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emitterMap.remove(key);
        }
    }

    private String buildTodayKey(int month, int day) {
        return month + "-" + day;
    }
}
