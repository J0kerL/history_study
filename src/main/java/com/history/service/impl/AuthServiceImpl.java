package com.history.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.history.exception.BusinessException;
import com.history.mapper.UserMapper;
import com.history.model.dto.LoginDTO;
import com.history.model.dto.RegisterDTO;
import com.history.model.entity.User;
import com.history.model.vo.LoginVO;
import com.history.model.vo.RegisterVO;
import com.history.service.AuthService;
import com.history.service.LearningRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类。
 * 负责登录、注册、短信验证码发送、刷新令牌和退出登录等认证相关能力。
 *
 * <p>其中：
 * <ul>
 *     <li>登录态由 Sa-Token 管理</li>
 *     <li>刷新令牌通过 SaTempUtil 维护</li>
 *     <li>短信验证码通过 Redis 缓存，并附带发送频率限制</li>
 * </ul>
 *
 * @author Diamond
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** Redis 中短信验证码的键前缀。 */
    private static final String VERIFICATION_CODE_KEY_PREFIX = "auth:sms:code:";
    /** Redis 中短信发送限流键前缀。 */
    private static final String VERIFICATION_CODE_LIMIT_KEY_PREFIX = "auth:sms:limit:";
    /** 验证码有效期，单位：分钟。 */
    private static final long VERIFICATION_CODE_EXPIRE_MINUTES = 5L;
    /** 验证码重复发送限制时间，单位：秒。 */
    private static final long VERIFICATION_CODE_LIMIT_SECONDS = 60L;
    /** 中国大陆 11 位手机号校验规则。 */
    private static final String PHONE_REGEX = "^\\d{11}$";

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private LearningRecordService learningRecordService;

    @Value("${sa-token.refresh-token-timeout:604800}")
    private long refreshTokenTimeout;

    @Value("${sa-token.refresh-token-single:true}")
    private boolean refreshTokenSingle;

    @Override
    /**
     * 用户登录。
     * 校验用户名和密码后，创建 Sa-Token 登录会话并签发 refreshToken。
     *
     * @param loginDTO 登录请求参数
     * @return 登录结果，包含 accessToken、refreshToken 和用户信息
     */
    public LoginVO login(LoginDTO loginDTO) {
        if (StrUtil.isBlank(loginDTO.getUsername()) || StrUtil.isBlank(loginDTO.getPassword())) {
            throw new BusinessException("用户名或密码不能为空");
        }

        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();

        // 记录学习行为（登录）
        checkAndRecordLoginLearning(user.getId());

        if (refreshTokenSingle) {
            for (String tempToken : SaTempUtil.getTempTokenList(user.getId())) {
                SaTempUtil.deleteToken(tempToken);
            }
        }

        String refreshToken = SaTempUtil.createToken(user.getId(), refreshTokenTimeout);
        return new LoginVO(accessToken, refreshToken, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 用户注册。
     * 会依次校验密码一致性、手机号格式、用户名唯一性、手机号唯一性以及短信验证码，
     * 校验通过后写入用户表，并清理已使用的验证码缓存。
     *
     * @param registerDTO 注册请求参数
     * @return 注册成功后的用户基础信息
     */
    public RegisterVO register(RegisterDTO registerDTO) {
        validateRegisterRequest(registerDTO);

        if (userMapper.selectByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (userMapper.selectByPhone(registerDTO.getPhone()) != null) {
            throw new BusinessException("手机号已注册");
        }

        String codeKey = buildVerificationCodeKey(registerDTO.getPhone());
        String cachedCode = getStringValue(codeKey);
        if (StrUtil.isBlank(cachedCode)) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!StrUtil.equals(cachedCode, registerDTO.getVerificationCode())) {
            throw new BusinessException("验证码错误");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setAvatar("https://history-study.oss-cn-beijing.aliyuncs.com/defaultAvatar.png");
        user.setRegisterDate(LocalDate.now());
        user.setStreakDays(0);
        user.setMaxStreakDays(0);
        user.setTotalQuizCount(0);
        user.setCorrectQuizCount(0);
        user.setStatus((byte) 1);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名或手机号已存在");
        }

        redisTemplate.delete(codeKey);
        return new RegisterVO(user.getId(), user.getUsername(), user.getPhone());
    }

    @Override
    /**
     * 发送手机验证码。
     * 先校验手机号格式，再通过 Redis 限制 1 分钟内重复发送。
     * 验证码仅做模拟发送：打印到控制台并写入 Redis。
     *
     * @param phone 手机号
     * @return 验证码有效期和重发间隔等元数据
     */
    public Map<String, Object> sendVerificationCode(String phone) {
        validatePhone(phone);

        String limitKey = buildVerificationCodeLimitKey(phone);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Boolean firstRequest = valueOperations.setIfAbsent(limitKey, "1", VERIFICATION_CODE_LIMIT_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(firstRequest)) {
            throw new BusinessException("验证码发送过于频繁，请1分钟后再试");
        }

        String verificationCode = RandomUtil.randomNumbers(6);
        String codeKey = buildVerificationCodeKey(phone);
        valueOperations.set(codeKey, verificationCode, VERIFICATION_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("手机号 {} 的注册验证码为：{}", phone, verificationCode);

        Map<String, Object> result = new HashMap<>();
        result.put("验证码为", verificationCode);
        result.put("过期时间", TimeUnit.MINUTES.toSeconds(VERIFICATION_CODE_EXPIRE_MINUTES));
        result.put("重新发送间隔时间", VERIFICATION_CODE_LIMIT_SECONDS);
        return result;
    }

    @Override
    /**
     * 刷新 accessToken。
     * 使用传入的 refreshToken 解析用户身份，并为该用户重新创建登录会话。
     *
     * @param refreshToken 刷新令牌
     * @return 新的 accessToken、原 refreshToken 及过期时间
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            Object userId = SaTempUtil.parseToken(refreshToken);
            User user = userMapper.selectById(Long.parseLong(String.valueOf(userId)));
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            if (user.getStatus() == 0) {
                throw new BusinessException("用户已被禁用");
            }

            String newAccessToken = StpUtil.createLoginSession(userId);
            long expiresIn = StpUtil.getTokenTimeout(newAccessToken);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("refreshToken", refreshToken);
            data.put("expiresIn", expiresIn);
            return data;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "刷新Token失败，请重新登录", e);
        }
    }

    @Override
    /**
     * 退出登录。
     * 先删除当前用户关联的 refreshToken，再注销当前 accessToken 对应的登录会话。
     */
    public void logout() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId != null) {
            for (String tempToken : SaTempUtil.getTempTokenList(loginId)) {
                SaTempUtil.deleteToken(tempToken);
            }
        }
        StpUtil.logout();
    }

    /**
     * 校验注册请求的基础参数。
     *
     * @param registerDTO 注册请求参数
     */
    private void validateRegisterRequest(RegisterDTO registerDTO) {
        if (!StrUtil.equals(registerDTO.getPassword(), registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        validatePhone(registerDTO.getPhone());
    }

    /**
     * 校验手机号格式是否正确。
     *
     * @param phone 手机号
     */
    private void validatePhone(String phone) {
        if (StrUtil.isBlank(phone) || !phone.matches(PHONE_REGEX)) {
            throw new BusinessException("手机号格式不正确");
        }
    }

    /**
     * 从 Redis 中读取字符串值。
     *
     * @param key Redis 键
     * @return 对应的字符串值，不存在时返回 {@code null}
     */
    private String getStringValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 生成验证码缓存键。
     *
     * @param phone 手机号
     * @return 验证码 Redis 键
     */
    private String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }

    /**
     * 生成验证码发送限流键。
     *
     * @param phone 手机号
     * @return 限流 Redis 键
     */
    private String buildVerificationCodeLimitKey(String phone) {
        return VERIFICATION_CODE_LIMIT_KEY_PREFIX + phone;
    }

    /**
     * 检查并记录登录学习行为。
     * 如果今天尚未记录过学习行为，则记录一次登录行为。
     *
     * @param userId 用户ID
     */
    private void checkAndRecordLoginLearning(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // 检查今天是否已经有学习记录
            boolean hasTodayRecord = learningRecordService.hasLearningRecord(userId, today);
            if (hasTodayRecord) {
                return; // 今日已记录，无需重复
            }

            // 检查昨天是否有学习记录（判断是否断签）
            boolean hasYesterdayRecord = learningRecordService.hasLearningRecord(userId, yesterday);
            if (!hasYesterdayRecord) {
                // 昨天没学习，检查是否有更早的记录
                LocalDate latestDate = learningRecordService.getLatestLearnDate(userId);
                if (latestDate != null && !latestDate.equals(today)) {
                    // 有历史记录但不是昨天，说明断签了，重置连续天数
                    User user = userMapper.selectById(userId);
                    if (user != null && user.getStreakDays() != null && user.getStreakDays() > 0) {
                        userMapper.updateStreakDays(userId, 0);
                        log.info("用户断签，重置连续学习天数: userId={}, latestDate={}", userId, latestDate);
                    }
                }
            }

            // 记录今天的登录学习行为
            learningRecordService.recordLearningAction(userId, (byte) 1);
        } catch (Exception e) {
            log.error("记录登录学习行为失败: userId={}", userId, e);
            // 不影响登录流程，仅记录日志
        }
    }
}
