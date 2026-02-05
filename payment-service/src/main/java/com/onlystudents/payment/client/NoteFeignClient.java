package com.onlystudents.payment.client;

import com.onlystudents.common.result.Result;
import com.onlystudents.payment.client.fallback.NoteFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 笔记服务 Feign 客户端
 * 用于 payment-service 调用 note-service 获取笔记信息
 */
@FeignClient(
        name = "note-service",
        fallback = NoteFeignClientFallback.class
)
public interface NoteFeignClient {

    /**
     * 根据笔记ID获取笔记信息
     *
     * @param noteId 笔记ID
     * @return 笔记信息（包含 userId 等字段）
     */
    @GetMapping("/note/{noteId}")
    Result<Map<String, Object>> getNoteById(@PathVariable("noteId") Long noteId);
}
