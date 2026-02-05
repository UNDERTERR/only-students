package com.onlystudents.payment.client.fallback;

import com.onlystudents.common.result.Result;
import com.onlystudents.payment.client.NoteFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 笔记服务 Feign 客户端降级处理类
 * 当 note-service 不可用时，返回降级数据
 */
@Slf4j
@Component
public class NoteFeignClientFallback implements NoteFeignClient {

    @Override
    public Result<Map<String, Object>> getNoteById(Long noteId) {
        log.error("调用 note-service 失败，执行降级处理，noteId={}", noteId);
        
        // 构建降级笔记信息
        Map<String, Object> fallbackNote = new HashMap<>();
        fallbackNote.put("id", noteId);
        fallbackNote.put("userId", null);  // 无法获取创作者ID
        fallbackNote.put("title", "笔记_" + noteId);
        
        return Result.success(fallbackNote);
    }
}
