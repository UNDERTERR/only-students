package com.onlystudents.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "sms:verify:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;

    /**
     * 验证码类型
     */
    public enum CodeType {
        REGISTER,    // 注册
        LOGIN,       // 登录
        RESET_PWD    // 忘记密码
    }

    /**
     * 发送验证码
     * @param target 邮箱或手机号
     * @param type 验证码类型
     * @return 生成的验证码（模拟发送，实际应调用短信/邮箱服务）
     *
     */
    public String sendCode(String target, CodeType type) {
        // 检查是否频繁发送（1分钟内只能发一次）
        String key = PREFIX + type.name() + ":" + target;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            // 检查是否还在冷却期
            String tempKey = PREFIX + "temp:" + type.name() + ":" + target;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(tempKey))) {
                throw new RuntimeException("发送过于频繁，请" + redisTemplate.getExpire(tempKey, TimeUnit.SECONDS) + "秒后重试");
            }
        }

        // 生成6位数字验证码
        String code = generateCode();

        // 存储验证码（5分钟过期）
        redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        // 存储发送冷却标记（1分钟）
        String tempKey = PREFIX + "temp:" + type.name() + ":" + target;
        redisTemplate.opsForValue().set(tempKey, "1", 1, TimeUnit.MINUTES);

        // 模拟发送（实际应调用短信/邮箱服务）
        log.info("【验证码模拟发送】目标: {}, 类型: {}, 验证码: {}", target, type, code);

        return code;
    }

    /**
     * 验证验证码
     * @param target 邮箱或手机号
     * @param code 验证码
     * @param type 验证码类型
     * @return 是否验证成功
     */
    public boolean verifyCode(String target, String code, CodeType type) {
        String key = PREFIX + type.name() + ":" + target;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            log.warn("验证码已过期或不存在: {}", target);
            return false;
        }

        if (savedCode.equals(code)) {
            // 验证成功后删除验证码（防止重复使用）
            redisTemplate.delete(key);
            log.info("验证码验证成功: {}", target);
            return true;
        }

        log.warn("验证码错误: 输入{} vs 存储{}", code, savedCode);
        return false;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
