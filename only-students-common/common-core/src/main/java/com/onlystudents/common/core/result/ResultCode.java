package com.onlystudents.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    
    // 参数错误
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISS(400, "缺少必要参数"),
    PARAM_TYPE_ERROR(400, "参数类型错误"),
    
    // 认证授权
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    TOKEN_EXPIRED(401, "Token已过期"),
    TOKEN_INVALID(401, "Token无效"),
    
    // 资源相关
    NOT_FOUND(404, "资源不存在"),
    RESOURCE_EXISTS(409, "资源已存在"),
    
    // 业务相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_EXISTS(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    USERNAME_PASSWORD_ERROR(1004, "用户名或密码错误"),
    CAPTCHA_ERROR(1005, "验证码错误"),
    
    NOTE_NOT_FOUND(2001, "笔记不存在"),
    NOTE_NOT_VISIBLE(2002, "笔记不可见"),
    
    SUBSCRIPTION_REQUIRED(3001, "需要订阅"),
    PAYMENT_REQUIRED(3002, "需要付费"),
    INSUFFICIENT_BALANCE(3003, "余额不足"),
    
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_CONVERT_ERROR(4002, "文件转换失败"),
    FILE_NOT_FOUND(4003, "文件不存在"),
    FILE_TOO_LARGE(4004, "文件过大"),
    
    // 服务相关
    SERVICE_ERROR(5000, "服务调用异常"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用");
    
    private final Integer code;
    private final String message;
}
