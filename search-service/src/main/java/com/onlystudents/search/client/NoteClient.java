package com.onlystudents.search.client;

import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.Result;
import com.onlystudents.search.dto.NoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Note-Service 客户端
 * 用于获取笔记数据并同步到 ES
 */
@FeignClient(name = "note-service", url = "${note-service.url:http://localhost:8003}")
public interface NoteClient {
    
    /**
     * 获取已发布的笔记（用于同步到 ES）
     * 返回包含用户信息的完整笔记数据
     */
    @GetMapping("/note/published")
    Result<List<NoteResponse>> getPublishedNotes(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    );
}