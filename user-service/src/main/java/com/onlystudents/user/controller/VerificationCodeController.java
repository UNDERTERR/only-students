package com.onlystudents.user.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.user.dto.SendCodeRequest;
import com.onlystudents.user.service.VerificationCodeService;
import com.onlystudents.user.service.VerificationCodeService.CodeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "验证码管理")
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@Validated
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    @PostMapping("/send")
    @Operation(summary = "发送验证码", description = "向邮箱或手机号发送验证码")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            CodeType type = CodeType.valueOf(request.getType().toUpperCase());
            verificationCodeService.sendCode(request.getAccount(), type);
            return Result.success();
        } catch (IllegalArgumentException e) {
            log.error("验证码类型错误: {}", request.getType());
            return Result.error("验证码类型错误");
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
