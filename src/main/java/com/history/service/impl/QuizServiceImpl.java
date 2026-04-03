package com.history.service.impl;

import com.history.exception.BusinessException;
import com.history.llm.LlmClient;
import com.history.mapper.AchievementMapper;
import com.history.mapper.QuizMapper;
import com.history.mapper.UserMapper;
import com.history.model.entity.*;
import com.history.model.vo.QuizAnswerResultVO;
import com.history.model.vo.QuizHistoryVO;
import com.history.model.vo.QuizStatsVO;
import com.history.model.vo.TodayQuizVO;
import com.history.service.AchievementService;
import com.history.service.QuizService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuizServiceImpl implements QuizService {

    private static final int QUIZ_INITIAL_COUNT = 100;
    private static final int QUIZ_HISTORY_LIMIT = 50;
    private static final int RECENT_ANSWER_COUNT = 5;
    private static final Byte AI_SOURCE = 2;
    private static final Byte STATUS_ONLINE = 1;

    @Resource
    private QuizMapper quizMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AchievementMapper achievementMapper;

    @Resource
    private LlmClient llmClient;

    @Resource
    private AchievementService achievementService;

    @Value("${quiz.generate-on-startup:true}")
    private boolean generateOnStartup;

    @Override
    public TodayQuizVO getTodayQuiz(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 获取今日题目，不存在则自动补生
        Long quizId = quizMapper.selectTodayQuizByDate(today);
        if (quizId == null) {
            log.info("今日题目缺失，自动补生");
            generateDailyQuizForDate(today);
            quizId = quizMapper.selectTodayQuizByDate(today);
        }
        if (quizId == null) {
            throw new BusinessException("今日题目生成失败，请稍后再试");
        }

        // 2. 查询题目详情
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null || !STATUS_ONLINE.equals(quiz.getStatus())) {
            throw new BusinessException("题目数据异常");
        }

        // 3. 检查用户是否已答
        UserQuizRecord record = quizMapper.selectUserTodayRecord(userId, today);
        boolean answered = record != null;

        TodayQuizVO vo = new TodayQuizVO();
        vo.setId(quiz.getId());
        vo.setQuestion(quiz.getQuestion());
        vo.setQuizType(quiz.getQuizType());
        vo.setOptionA(quiz.getOptionA());
        vo.setOptionB(quiz.getOptionB());
        vo.setOptionC(quiz.getOptionC());
        vo.setOptionD(quiz.getOptionD());
        vo.setDifficulty(quiz.getDifficulty());
        vo.setAnswered(answered);

        // 4. 若已答，一并返回答题详情，供前端恢复状态
        if (answered) {
            vo.setCorrectOptions(quiz.getCorrectOptions());
            vo.setSelectedOptions(record.getSelectedOptions());
            vo.setCorrect(record.getIsCorrect() != null && record.getIsCorrect() == 1);
            vo.setExplanation(quiz.getExplanation());
        }

        return vo;
    }

    @Override
    public QuizAnswerResultVO submitAnswer(Long userId, Long quizId, String selectedOptions) {
        LocalDate today = LocalDate.now();

        // 1. 校验是否重复提交
        UserQuizRecord existing = quizMapper.selectUserTodayRecord(userId, today);
        if (existing != null) {
            Quiz quiz = quizMapper.selectById(existing.getQuizId());
            return buildAnswerResult(quiz, existing.getSelectedOptions(), existing.getIsCorrect());
        }

        // 2. 查询正确答案
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException("题目不存在");
        }

        boolean correct = isAnswerCorrect(quiz, selectedOptions);
        byte isCorrect = correct ? (byte) 1 : (byte) 0;

        // 3. 保存答题记录
        UserQuizRecord record = new UserQuizRecord();
        record.setUserId(userId);
        record.setQuizId(quizId);
        record.setSelectedOptions(selectedOptions);
        record.setIsCorrect(isCorrect);
        record.setAnswerDate(today);
        quizMapper.insertQuizRecord(record);

        // 4. 更新用户学习统计
        updateUserStats(userId, correct);

        // 5. 检查并解锁成就
        checkAndUnlockAchievements(userId);

        return buildAnswerResult(quiz, selectedOptions, isCorrect);
    }

    @Override
    public QuizStatsVO getStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        QuizStatsVO vo = new QuizStatsVO();
        vo.setStreakDays(user.getStreakDays());
        vo.setMaxStreakDays(user.getMaxStreakDays());
        vo.setTotalQuizCount(user.getTotalQuizCount());
        vo.setCorrectQuizCount(user.getCorrectQuizCount());

        if (user.getTotalQuizCount() != null && user.getTotalQuizCount() > 0) {
            int total = user.getTotalQuizCount();
            int correct = user.getCorrectQuizCount() != null ? user.getCorrectQuizCount() : 0;
            vo.setAccuracyRate(Math.round(correct * 1000.0 / total) / 10.0);
        } else {
            vo.setAccuracyRate(0.0);
        }
        return vo;
    }

    @Override
    public List<QuizHistoryVO> getQuizHistory(Long userId, int limit) {
        List<Map<String, Object>> records = quizMapper.selectUserQuizHistory(userId, limit);
        return records.stream().map(row -> {
            QuizHistoryVO vo = new QuizHistoryVO();
            vo.setAnswerDate((LocalDate) row.get("answerDate"));
            vo.setQuestion((String) row.get("question"));
            vo.setQuizType((Byte) row.get("quizType"));
            vo.setCorrectOptions((String) row.get("correctOptions"));
            vo.setSelectedOptions((String) row.get("selectedOptions"));
            Byte isCorrect = (Byte) row.get("isCorrect");
            vo.setCorrect(isCorrect != null && isCorrect == 1);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public int generateQuizBatch(int count) {
        int unusedCount = quizMapper.countUnusedQuiz();
        if (unusedCount < 20) {
            int toGenerate = 100 - unusedCount;
            log.info("未使用题目仅剩 {} 道，开始批量生成 {} 道", unusedCount, toGenerate);
            List<Quiz> allGenerated = new ArrayList<>();
            int batchSize = 10;
            int batches = (toGenerate + batchSize - 1) / batchSize;
            for (int i = 0; i < batches; i++) {
                int remaining = toGenerate - i * batchSize;
                int currentBatch = Math.min(remaining, batchSize);

                QuizBatchResult batchResult = generateQuizBatchByLlm(currentBatch, i * batchSize);
                if (batchResult == null || batchResult.getQuizzes() == null || batchResult.getQuizzes().isEmpty()) {
                    log.warn("第 {} 批题目生成失败，跳过", i + 1);
                    continue;
                }

                List<Quiz> quizzes = batchResult.getQuizzes();
                for (Quiz quiz : quizzes) {
                    quiz.setStatus(STATUS_ONLINE);
                    quiz.setSource(AI_SOURCE);
                }
                allGenerated.addAll(quizzes);
            }

            if (allGenerated.isEmpty()) {
                log.warn("题库批量生成结果为空");
                return 0;
            }

            quizMapper.batchInsert(allGenerated);
            log.info("题库批量生成完成，共落库 {} 道题", allGenerated.size());
            return allGenerated.size();
        }
        log.info("未使用题目已有 {} 道，跳过自动生成。", unusedCount);
        return 0;
    }

    private QuizBatchResult generateQuizBatchByLlm(int count, int offset) {
        // 根据 offset 调整 prompt，确保覆盖不同主题
        String topicHint = switch (offset / 10) {
            case 0 -> "朝代更替与政治制度变革";
            case 1 -> "军事战争与外交";
            case 2 -> "文化科技与文学艺术";
            case 3 -> "经济民生与社会制度";
            default -> "综合各时期各类型";
        };

        String systemPrompt =
                "你是中国历史知识竞赛的出题专家。请根据中国历史出选择题，要求题目热门且不偏门，"
                + "覆盖大众熟知的朝代、人物和事件。"
                + "必须返回严格 JSON，字段结构为 {\"quizzes\":[{\"question\":\"题目内容\",\"quizType\":1或2,\"optionA\":\"A选项\",\"optionB\":\"B选项\",\"optionC\":\"C选项\",\"optionD\":\"D选项\",\"correctOptions\":\"正确答案（如 A 或 AB 或 ABC）\",\"explanation\":\"答案解析\",\"difficulty\":1或2或3,\"tags\":\"标签1,标签2\"}]}。"
                + "难度分布要求：50% 简单(difficulty=1)，30% 中等(difficulty=2)，20% 困难(difficulty=3)。"
                + "quizType：1 表示单选，2 表示多选，随机分配。"
                + "不要输出 Markdown，不要输出解释。";

        String userPrompt = "请生成 " + count + " 道中国历史选择题，主题侧重" + topicHint + "。";

        return llmClient.call(systemPrompt, userPrompt, QuizBatchResult.class);
    }

    private void generateDailyQuizForDate(LocalDate date) {
        Quiz quiz = quizMapper.selectUnusedQuiz(null);
        if (quiz == null) {
            log.warn("题库耗尽，尝试补充生成 100 道题");
            int generated = generateQuizBatch(100);
            if (generated > 0) {
                quiz = quizMapper.selectUnusedQuiz(null);
            }
            if (quiz == null) {
                log.warn("题库补充生成后仍无可用题目");
                return;
            }
        }
        quizMapper.insertDailyQuiz(date, quiz.getId());
        log.info("每日一题已生成: date={}, quizId={}, difficulty={}", date, quiz.getId(), quiz.getDifficulty());
    }

    /**
     * 启动时自动生成题库（可通过配置关闭）。
     */
    @PostConstruct
    public void initQuizPool() {
        if (generateOnStartup) {
            try {
                generateQuizBatch(QUIZ_INITIAL_COUNT);
            } catch (Exception e) {
                log.error("启动时自动生成题库失败", e);
            }
        }
    }

    // ===== 私有方法 =====

    private QuizAnswerResultVO buildAnswerResult(Quiz quiz, String selectedOptions, byte isCorrect) {
        QuizAnswerResultVO vo = new QuizAnswerResultVO();
        vo.setQuizId(quiz.getId());
        vo.setCorrectOptions(quiz.getCorrectOptions());
        vo.setSelectedOptions(selectedOptions);
        vo.setCorrect(isCorrect == 1);
        vo.setExplanation(quiz.getExplanation());
        return vo;
    }

    private boolean isAnswerCorrect(Quiz quiz, String selectedOptions) {
        return quiz.getCorrectOptions().equalsIgnoreCase(selectedOptions.toUpperCase().replaceAll("\\s+", ""));
    }

    private void updateUserStats(Long userId, boolean correct) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        int totalCount = user.getTotalQuizCount() != null ? user.getTotalQuizCount() + 1 : 1;
        int correctCount = user.getCorrectQuizCount() != null
                ? (correct ? user.getCorrectQuizCount() + 1 : user.getCorrectQuizCount())
                : (correct ? 1 : 0);
        int streakDays = user.getStreakDays() != null ? user.getStreakDays() + 1 : 1;
        int maxStreakDays = Math.max(streakDays,
                user.getMaxStreakDays() != null ? user.getMaxStreakDays() : streakDays);

        user.setTotalQuizCount(totalCount);
        user.setCorrectQuizCount(correctCount);
        user.setStreakDays(streakDays);
        user.setMaxStreakDays(maxStreakDays);
        userMapper.updateUserQuizStats(userId, totalCount, correctCount, streakDays, maxStreakDays);
    }

    private void checkAndUnlockAchievements(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        // 检查所有未解锁成就
        List<Achievement> allAchievements = achievementMapper.selectAll();
        // 简化实现：逐个检查条件并解锁
        List<UserAchievement> unlocked = achievementMapper.selectByUserId(userId);
        List<Integer> unlockedIds = unlocked.stream()
                .map(UserAchievement::getAchievementId)
                .toList();

        if (allAchievements != null) {
            for (Achievement achievement : allAchievements) {
                if (achievement == null || unlockedIds.contains(achievement.getId())) {
                    continue;
                }

                boolean shouldUnlock = false;
                switch (achievement.getConditionType()) {
                    case 1: // 答题总数
                        shouldUnlock = user.getTotalQuizCount() != null
                                && user.getTotalQuizCount() >= achievement.getConditionValue();
                        break;
                    case 2: // 答对总数
                        shouldUnlock = user.getCorrectQuizCount() != null
                                && user.getCorrectQuizCount() >= achievement.getConditionValue();
                        break;
                    case 3: // 连续学习天数
                        shouldUnlock = user.getStreakDays() != null
                                && user.getStreakDays() >= achievement.getConditionValue();
                        break;
                    default:
                        break;
                }

                if (shouldUnlock) {
                    achievementService.unlockAchievement(userId, achievement.getId());
                    log.info("用户解锁成就: userId={}, achievementId={}, name={}",
                            userId, achievement.getId(), achievement.getName());
                }
            }
        }
    }

    /**
     * LLM 批量题目结果映射。
     */
    private static class QuizBatchResult {
        private List<Quiz> quizzes;

        public List<Quiz> getQuizzes() {
            return quizzes;
        }

        public void setQuizzes(List<Quiz> quizzes) {
            this.quizzes = quizzes;
        }
    }
}
