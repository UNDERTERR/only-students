package com.onlystudents.comment.client.fallback;

import com.onlystudents.comment.client.NoteFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NoteFeignClientFallbackFactory implements FallbackFactory<NoteFeignClient> {
    
    @Override
    public NoteFeignClient create(Throwable throwable) {
        log.error("NoteFeignClient 调用失败: {}", throwable.getMessage());
        return new NoteFeignClient() {
            @Override
            public Result<Map<String, Object>> getNoteById(Long id) {
                return Result.error("获取笔记信息失败");
            }
            
            @Override
            public Result<List<Long>> getNoteIdsByUserId(Long userId) {
                return Result.success(Collections.emptyList());
            }
        };
    }
}
