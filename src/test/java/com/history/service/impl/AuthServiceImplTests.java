package com.history.service.impl;

import com.history.exception.BusinessException;
import com.history.mapper.UserMapper;
import com.history.model.dto.RegisterDTO;
import com.history.model.entity.User;
import com.history.model.vo.RegisterVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(authService, "refreshTokenTimeout", 604800L);
        ReflectionTestUtils.setField(authService, "refreshTokenSingle", true);
    }

    @Test
    void registerShouldFailWhenPasswordsDoNotMatch() {
        RegisterDTO registerDTO = buildRegisterDTO();
        registerDTO.setConfirmPassword("654321");

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("两次输入的密码不一致");
    }

    @Test
    void registerShouldFailWhenVerificationCodeIsInvalid() {
        RegisterDTO registerDTO = buildRegisterDTO();
        when(userMapper.selectByUsername(registerDTO.getUsername())).thenReturn(null);
        when(userMapper.selectByPhone(registerDTO.getPhone())).thenReturn(null);
        when(valueOperations.get("auth:sms:code:" + registerDTO.getPhone())).thenReturn("654321");

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码错误");
    }

    @Test
    void registerShouldPersistUserAndClearCode() {
        RegisterDTO registerDTO = buildRegisterDTO();
        when(userMapper.selectByUsername(registerDTO.getUsername())).thenReturn(null);
        when(userMapper.selectByPhone(registerDTO.getPhone())).thenReturn(null);
        when(valueOperations.get("auth:sms:code:" + registerDTO.getPhone())).thenReturn("123456");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L);
            return 1;
        });
        when(redisTemplate.delete("auth:sms:code:" + registerDTO.getPhone())).thenReturn(true);

        RegisterVO registerVO = authService.register(registerDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("history_user");
        assertThat(savedUser.getPhone()).isEqualTo("13800138000");
        assertThat(savedUser.getRegisterDate()).isEqualTo(LocalDate.now());
        assertThat(savedUser.getPassword()).isNotEqualTo("123456");
        assertThat(registerVO.getId()).isEqualTo(100L);
        assertThat(registerVO.getUsername()).isEqualTo("history_user");
        verify(redisTemplate).delete("auth:sms:code:" + registerDTO.getPhone());
    }

    @Test
    void sendVerificationCodeShouldFailWhenRequestedTooFrequently() {
        when(valueOperations.setIfAbsent(eq("auth:sms:limit:13800138000"), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.sendVerificationCode("13800138000"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码发送过于频繁，请1分钟后再试");
    }

    @Test
    void sendVerificationCodeShouldStoreCodeAndReturnMetadata() {
        when(valueOperations.setIfAbsent(eq("auth:sms:limit:13800138000"), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        doNothing().when(valueOperations).set(eq("auth:sms:code:13800138000"), any(), eq(5L), eq(TimeUnit.MINUTES));

        Map<String, Object> result = authService.sendVerificationCode("13800138000");

        verify(valueOperations).set(eq("auth:sms:code:13800138000"), any(), eq(5L), eq(TimeUnit.MINUTES));
        assertThat(result).containsEntry("expireSeconds", 300L);
        assertThat(result).containsEntry("resendIntervalSeconds", 60L);
        verify(userMapper, never()).insert(any(User.class));
    }

    private RegisterDTO buildRegisterDTO() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("history_user");
        registerDTO.setPassword("123456");
        registerDTO.setConfirmPassword("123456");
        registerDTO.setPhone("13800138000");
        registerDTO.setVerificationCode("123456");
        return registerDTO;
    }
}
