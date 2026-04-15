package com.history.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.history.exception.BusinessException;
import com.history.llm.LlmClient;
import com.history.mapper.DailyRecommendationMapper;
import com.history.mapper.FigureMapper;
import com.history.model.ai.GeneratedFigure;
import com.history.model.entity.DailyRecommendation;
import com.history.model.entity.Figure;
import com.history.model.vo.DailyRecommendationVO;
import com.history.model.vo.FigureDetailVO;
import com.history.service.FigureRecommendationService;
import com.history.service.OssService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.Lazy;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 每日人物推荐 Service 实现。
 * 采用预生成策略：启动时检查库存，不足则批量生成。
 *
 * @author Diamond
 */
@Slf4j
@Service
public class FigureRecommendationServiceImpl implements FigureRecommendationService {

    private static final byte STATUS_ONLINE = 1;

    /** 人物库存阈值，低于此值时触发批量生成 */
    private static final int FIGURE_INVENTORY_THRESHOLD = 3;

    /** 批量生成的默认数量 */
    private static final int BATCH_GENERATE_COUNT = 5;

    /** 防止并发重复生成 */
    private final ConcurrentHashMap<String, Boolean> generationLocks = new ConcurrentHashMap<>();

    @Resource
    private FigureMapper figureMapper;

    @Resource
    private DailyRecommendationMapper dailyRecommendationMapper;

    @Resource
    private LlmClient llmClient;

    @Resource
    private EventImageService eventImageService;

    @Resource
    private OssService ossService;

    /** 使用 @Lazy 避免循环依赖，通过 Spring 代理调用以触发 @Async 切面。 */
    @Lazy
    @Resource
    private FigureAsyncService figureAsyncService;

    @Override
    public DailyRecommendationVO getTodayRecommendation() {
        LocalDate today = LocalDate.now();

        // 1. 查询今日是否已有推荐
        DailyRecommendation recommendation = dailyRecommendationMapper.selectByDate(today);

        if (recommendation != null && recommendation.getContentId() != null) {
            // 已有推荐，直接返回
            return buildRecommendationVO(recommendation);
        }

        // 2. 无推荐，从库存中选取未推荐过的人物
        log.info("今日人物推荐缺失，从库存中选取");
        List<Figure> candidates = figureMapper.selectUnrecommendedFigures(1);

        if (candidates == null || candidates.isEmpty()) {
            // 库存耗尽，批量生成补充库存
            log.warn("人物库存已耗尽，开始批量生成{}个人物", BATCH_GENERATE_COUNT);
            int generated = generateFiguresBatch(BATCH_GENERATE_COUNT);
            if (generated == 0) {
                throw new BusinessException("人物生成失败，请稍后再试");
            }
            // 重新查询新入库的人物
            candidates = figureMapper.selectUnrecommendedFigures(1);
            if (candidates == null || candidates.isEmpty()) {
                throw new BusinessException("人物生成失败，请稍后再试");
            }
        } else {
            // 使用库存中的人物
            Figure figure = candidates.get(0);
            recommendation = saveRecommendation(today, figure);
            
            // 检查未推荐人物库存是否低于阈值，如是则异步补充（使用线程池，避免裸 new Thread）
            int unrecommendedCount = figureMapper.countUnrecommendedFigures();
            if (unrecommendedCount < FIGURE_INVENTORY_THRESHOLD) {
                log.info("未推荐人物库存不足（剩余{}，阈值{}），异步批量生成{}个",
                        unrecommendedCount, FIGURE_INVENTORY_THRESHOLD, BATCH_GENERATE_COUNT);
                figureAsyncService.replenishFiguresAsync(BATCH_GENERATE_COUNT);
            }
        }

        return buildRecommendationVO(recommendation);
    }

    @Override
    public int generateFiguresBatch(int count) {
        String lockKey = "batch_generate";
        if (Boolean.FALSE.equals(generationLocks.putIfAbsent(lockKey, true))) {
            log.info("批量生成任务已在进行中");
            return 0;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        try {
            // 获取已有人物名单，避免重复生成
            List<String> existingNames = getExistingFigureNames();
            log.info("已存在{}个人物，批量生成时将避免重复", existingNames.size());
            
            for (int i = 0; i < count; i++) {
                try {
                    Figure figure = generateSingleFigure(existingNames);
                    if (figure != null) {
                        successCount.incrementAndGet();
                        existingNames.add(figure.getName()); // 加入已生成名单
                        log.info("批量生成进度: {}/{}", i + 1, count);
                    }
                } catch (Exception e) {
                    log.error("批量生成第{}个人物失败", i + 1, e);
                }
            }
            log.info("批量生成完成，成功: {}/{}", successCount.get(), count);
        } finally {
            generationLocks.remove(lockKey);
        }

        return successCount.get();
    }

    @Override
    public FigureDetailVO getFigureDetail(Long id) {
        Figure figure = figureMapper.selectById(id);
        if (figure == null) {
            throw new BusinessException("人物不存在");
        }
        return buildFigureDetailVO(figure);
    }

    /**
     * 生成单个人物（LLM + 图片）。
     */
    private Figure generateSingleFigure(List<String> existingNames) {
        // 1. 调用 LLM 生成人物数据
        GeneratedFigure generated = callLlmToGenerateFigure(existingNames);
        if (generated == null) {
            log.error("LLM 生成人物数据失败");
            return null;
        }

        // 2. 生成人物画像
        String imageUrl = generateFigureImage(generated.getImagePrompt(), generated.getName());

        // 3. 构建实体
        Figure figure = new Figure();
        figure.setName(generated.getName());
        figure.setSubtitle(generated.getSubtitle());
        figure.setBirthDate(generated.getBirthDate());
        figure.setDeathDate(generated.getDeathDate());
        figure.setDynasty(generated.getDynasty());
        figure.setBirthPlace(generated.getBirthPlace());
        figure.setBiography(generated.getBiography());
        figure.setWorks(generated.getWorks());
        figure.setImageUrl(imageUrl);

        // 4. 入库
        try {
            figureMapper.insert(figure);
            log.info("人物入库成功: name={}, id={}", figure.getName(), figure.getId());
            return figure;
        } catch (Exception e) {
            log.error("人物入库失败: name={}", figure.getName(), e);
            return null;
        }
    }

    /**
     * 调用 LLM 生成历史人物数据。
     */
    private GeneratedFigure callLlmToGenerateFigure(List<String> existingNames) {
        // 构建已有人物提示
        String existingNamesHint = "";
        if (existingNames != null && !existingNames.isEmpty()) {
            existingNamesHint = "\n\n已存在以下人物，请务必避免重复：" + String.join("、", existingNames);
        }
        
        String systemPrompt = """
            你是中国历史研究专家，擅长撰写详实、准确的历史人物传记。
            请严格按照 JSON 格式输出，不要包含任何其他文字。
            
            要求：
            1. 人物必须是中国古代历史人物（1911年清朝及之前）
            2. 每次必须生成不同的人物，不要重复
            3. 优先选择各朝代的代表性人物（帝王将相、文人墨客、科学家、军事家等）
            4. 传记内容必须详实、准确，至少800字，分3-5个段落
            5. 包含人物生平、主要成就、历史影响等
            6. 内容必须真实，不得虚构
            7. tags 用JSON数组格式，如：["文学家","书法家","唐宋八大家"]
            8. achievements 用逗号分隔的主要成就，如：开创豪放词派,散文大家,宋四家之一
            9. works 用逗号分隔的代表作品，如：赤壁赋,念奴娇·赤壁怀古,水调歌头
            10. imagePrompt 用中文描述人物外貌特征和场景，用于生成人物画像，要求：中国古代人物肖像画，传统服饰，历史氛围，写实风格，高质量细节
            """ + existingNamesHint;

        String userPrompt = """
            请生成一位中国古代历史人物的详细资料，确保：
            - 每次生成不同的人物，避免重复
            - 涵盖不同朝代、不同领域（政治、军事、文学、科学、艺术等）
            - 内容真实准确
            - 传记详尽（至少800字）
            - 涵盖生平、成就、影响
            
            输出 JSON 格式：
            {
              "name": "人物姓名",
              "subtitle": "身份描述，如：北宋文学家、书法家、画家",
              "birthDate": "出生日期，如：1037年1月8日",
              "deathDate": "逝世日期，如：1101年8月24日",
              "dynasty": "朝代，如：北宋",
              "birthPlace": "出生地，如：眉州眉山（今四川省眉山市）",
              "biography": "详细传记内容，多段落，至少800字",
              "achievements": "主要成就，逗号分隔，如：开创豪放词派,散文大家,宋四家之一",
              "works": "代表作品，逗号分隔，如：赤壁赋,念奴娇·赤壁怀古,水调歌头",
              "tags": ["文学家", "书法家", "画家", "唐宋八大家"],
              "imagePrompt": "中文图片生成提示词，描述人物外貌、服饰、场景"
            }
            """;

        try {
            return llmClient.call(systemPrompt, userPrompt, GeneratedFigure.class);
        } catch (Exception e) {
            log.error("调用 LLM 生成人物失败", e);
            return null;
        }
    }

    /**
     * 生成人物画像。
     */
    private String generateFigureImage(String imagePrompt, String figureName) {
        if (StrUtil.isBlank(imagePrompt)) {
            log.warn("人物 {} 的 imagePrompt 为空，跳过图片生成", figureName);
            return null;
        }

        try {
            // 增强提示词
            String enhancedPrompt = imagePrompt + ", high quality, detailed, professional photography style, museum portrait";

            // 调用图片生成服务
            String imageUrl = eventImageService.generateEventImage(enhancedPrompt);

            if (StrUtil.isNotBlank(imageUrl)) {
                log.info("人物 {} 画像生成成功: {}", figureName, imageUrl);
                return imageUrl;
            } else {
                log.warn("人物 {} 画像生成失败", figureName);
                return null;
            }
        } catch (Exception e) {
            log.error("人物 {} 画像生成异常", figureName, e);
            return null;
        }
    }

    /**
     * 保存推荐记录。
     */
    private DailyRecommendation saveRecommendation(LocalDate date, Figure figure) {
        DailyRecommendation recommendation = new DailyRecommendation();
        recommendation.setRecommendDate(date);
        recommendation.setContentId(figure.getId());

        dailyRecommendationMapper.insert(recommendation);
        log.info("推荐记录已保存: date={}, figureId={}", date, figure.getId());

        return recommendation;
    }

    /**
     * 从 subtitle 中提取朝代信息。
     */
    private String extractDynastyFromSubtitle(String subtitle) {
        if (StrUtil.isBlank(subtitle)) {
            return "古代";
        }
        // 尝试匹配常见朝代
        String[] dynasties = {"夏", "商", "周", "春秋", "战国", "秦", "汉", "三国", "晋", "南北朝", 
                             "隋", "唐", "五代", "宋", "北宋", "南宋", "元", "明", "清"};
        for (String dynasty : dynasties) {
            if (subtitle.contains(dynasty)) {
                return dynasty;
            }
        }
        return "古代";
    }

    /**
     * 获取数据库中已存在的人物名称列表。
     */
    private List<String> getExistingFigureNames() {
        List<Figure> allFigures = figureMapper.selectAllFigures();
        return allFigures.stream()
                .map(Figure::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 构建推荐 VO。
     */
    private DailyRecommendationVO buildRecommendationVO(DailyRecommendation recommendation) {
        Figure figure = figureMapper.selectById(recommendation.getContentId());
        if (figure == null) {
            throw new BusinessException("人物数据异常");
        }

        DailyRecommendationVO vo = new DailyRecommendationVO();
        vo.setRecommendDate(recommendation.getRecommendDate().toString());
        vo.setFigure(buildFigureDetailVO(figure));

        return vo;
    }

    private FigureDetailVO buildFigureDetailVO(Figure figure) {
        FigureDetailVO figureVO = new FigureDetailVO();
        figureVO.setId(figure.getId());
        figureVO.setName(figure.getName());
        figureVO.setSubtitle(figure.getSubtitle());
        figureVO.setTimeRange(buildTimeRange(figure.getBirthDate(), figure.getDeathDate()));
        figureVO.setBirthPlace(figure.getBirthPlace());
        figureVO.setDynasty(figure.getDynasty());
        figureVO.setImageUrl(figure.getImageUrl());
        figureVO.setBiography(figure.getBiography());
        figureVO.setWorks(figure.getWorks());
        figureVO.setRecommendReason(extractDynastyFromSubtitle(figure.getSubtitle()));
        return figureVO;
    }

    private String buildTimeRange(String birthDate, String deathDate) {
        if (StrUtil.isBlank(birthDate) && StrUtil.isBlank(deathDate)) {
            return "";
        }
        if (StrUtil.isBlank(deathDate)) {
            return birthDate;
        }
        if (StrUtil.isBlank(birthDate)) {
            return deathDate;
        }
        return birthDate + " — " + deathDate;
    }
}
