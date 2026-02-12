package com.onlystudents.search.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.search.service.ElasticsearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ES 管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/es")
@RequiredArgsConstructor
@Tag(name = "ES管理", description = "Elasticsearch索引管理接口")
public class ElasticsearchController {
    
    private final ElasticsearchIndexService elasticsearchIndexService;
    
    @PostMapping("/init")
    @Operation(summary = "手动初始化ES索引", description = "手动触发ES索引初始化和数据同步")
    public Result<String> initializeIndexes() {
        try {
            log.info("手动触发ES索引初始化...");
            elasticsearchIndexService.initializeAllIndexes();
            return Result.success("ES索引初始化完成");
        } catch (Exception e) {
            log.error("ES索引初始化失败", e);
            return Result.error("初始化失败: " + e.getMessage());
        }
    }
}