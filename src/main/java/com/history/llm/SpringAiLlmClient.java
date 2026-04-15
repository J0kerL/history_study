package com.history.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.history.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 基于 Spring AI 的大模型客户端实现。
 * <ul>
 *   <li>支持自动重试（最多 {@value #MAX_RETRY_ATTEMPTS} 次），覆盖网络抖动和限流场景</li>
 *   <li>区分网络调用失败与 JSON 解析失败，便于问题定位</li>
 *   <li>JSON 归一化：自动去除 Markdown 代码块包裹，并修复 LLM 输出的非法内引号</li>
 * </ul>
 */
@Slf4j
@Component
public class SpringAiLlmClient implements LlmClient {

    /** 最大重试次数（不含首次），总计最多调用 MAX_RETRY_ATTEMPTS+1 次。 */
    private static final int MAX_RETRY_ATTEMPTS = 2;
    /** 重试基础等待时间（毫秒），实际等待为 BASE_RETRY_DELAY_MS * attempt。 */
    private static final long BASE_RETRY_DELAY_MS = 1000L;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public SpringAiLlmClient(ChatClient historyChatClient, ObjectMapper objectMapper) {
        this.chatClient = historyChatClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String call(String userPrompt) {
        return callWithRetry(() -> chatClient.prompt()
                .user(userPrompt)
                .call()
                .content(), "call(userPrompt)");
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        return callWithRetry(() -> chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content(), "call(systemPrompt, userPrompt)");
    }

    @Override
    public <T> T call(String systemPrompt, String userPrompt, Class<T> responseType) {
        String content = callWithRetry(() -> chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content(), "call(..., " + responseType.getSimpleName() + ")");

        try {
            return objectMapper.readValue(normalizeJson(content), responseType);
        } catch (Exception e) {
            // 区分解析失败：单独记录原始内容，便于排查 LLM 输出格式问题
            log.error("LLM 响应 JSON 解析失败, targetType={}, rawContent={}",
                    responseType.getSimpleName(), content, e);
            throw new BusinessException("大模型结构化输出解析失败", e);
        }
    }

    // ===== 重试核心 =====

    /**
     * 带重试的调用包装：网络/调用类异常触发重试，指数退避，超出上限后抛出 BusinessException。
     */
    private <T> T callWithRetry(LlmCallable<T> action, String context) {
        Exception lastException = null;
        for (int attempt = 0; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long delay = BASE_RETRY_DELAY_MS * (attempt + 1);
                    log.warn("LLM 调用失败 [{}]，{}/{} 次重试，等待 {}ms: {}",
                            context, attempt + 1, MAX_RETRY_ATTEMPTS, delay, e.getMessage());
                    sleepQuietly(delay);
                }
            }
        }
        log.error("LLM 调用最终失败 [{}]，已重试 {} 次", context, MAX_RETRY_ATTEMPTS, lastException);
        throw new BusinessException("大模型调用失败（已重试 " + MAX_RETRY_ATTEMPTS + " 次）", lastException);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface LlmCallable<T> {
        T call() throws Exception;
    }

    // ===== JSON 归一化 =====

    private String normalizeJson(String content) throws JsonProcessingException {
        if (content == null) {
            throw new JsonProcessingException("LLM 返回内容为空") {};
        }
        String json = content.trim();
        if (json.startsWith("```")) {
            json = stripMarkdownFence(json);
        }
        json = extractJsonObject(json);
        return escapeInnerQuotesInJson(json);
    }

    private String stripMarkdownFence(String content) {
        String stripped = content.replaceFirst("^```(?:json)?\\s*", "");
        return stripped.replaceFirst("\\s*```$", "").trim();
    }

    private String extractJsonObject(String content) throws JsonProcessingException {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new JsonProcessingException("LLM 输出中未找到 JSON 对象") {};
        }
        return content.substring(start, end + 1);
    }

    /**
     * 修复 LLM 输出中字符串值里的非法英文引号（LLM 有时忽略"禁用双引号"的 Prompt 指令）。
     *
     * <p>原始 bug：遇到 '"xxx":' 模式时，':'  被误判为 JSON 结构符，导致把值内部的引号当作闭合引号。
     *
     * <p>修复方案：引入嵌套上下文栈区分 object / array，仅在 object 的 key 位置才允许 ':' 作为闭合触发符。
     * 在 value 位置或 array 元素中，':' 始终视为普通字符，不触发闭合。
     */
    private String escapeInnerQuotesInJson(String json) {
        StringBuilder builder = new StringBuilder(json.length() + 32);
        boolean inString = false;
        boolean inKey = false;       // 当前字符串是否为 object 的 key
        boolean escaped = false;
        char lastStructural = '\0'; // 上一个非空白的结构字符（仅在字符串外更新）

        // 嵌套层级栈：true = 当前层是 object，false = 当前层是 array
        Deque<Boolean> contextStack = new ArrayDeque<>();

        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);

            // 字符串外：维护结构字符和上下文栈
            if (!inString) {
                switch (current) {
                    case '{' -> { contextStack.push(true);  lastStructural = current; }
                    case '[' -> { contextStack.push(false); lastStructural = current; }
                    case '}', ']' -> { if (!contextStack.isEmpty()) contextStack.pop(); lastStructural = current; }
                    case ':', ',' -> lastStructural = current;
                    default -> {
                        // 非结构字符（包括双引号、数字、负号等）不更新 lastStructural，
                        // 以保证 key/value 位置判断始终基于上一结构符号。
                    }
                }
            }

            if (current == '"' && !escaped) {
                if (!inString) {
                    // 开启字符串：判断是 key 还是 value
                    // key 条件：当前层是 object，且紧接在 { 或 , 之后
                    boolean inObject = !contextStack.isEmpty() && Boolean.TRUE.equals(contextStack.peek());
                    inKey = inObject && (lastStructural == '{' || lastStructural == ',');
                    inString = true;
                    builder.append(current);
                    continue;
                }

                char nextSig = findNextSignificantChar(json, i + 1);
                // 以下情况视为正常闭合引号：
                // 1. 后跟 , } ]（结构符，key/value 均适用）
                // 2. 后跟 :，且当前字符串是 key（value 中的 : 不触发闭合）
                boolean closingOnStructural = nextSig == ',' || nextSig == '}' || nextSig == ']';
                boolean closingOnColon = inKey && nextSig == ':';

                if (closingOnStructural || closingOnColon) {
                    inString = false;
                    builder.append(current);
                    continue;
                }

                // 内部引号：转义
                builder.append("\\\"");
                continue;
            }

            builder.append(current);
            if (inString) {
                escaped = (current == '\\') && !escaped;
            } else {
                escaped = false;
            }
        }

        return builder.toString();
    }

    private char findNextSignificantChar(String json, int fromIndex) {
        for (int i = fromIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (!Character.isWhitespace(c)) {
                return c;
            }
        }
        return '\0';
    }
}
