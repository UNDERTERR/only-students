package com.onlystudents.report.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记服务 Feign 客户端
 * 用于 report-service 调用 note-service 修改笔记状态
 */
@FeignClient(name = "note-service")
public interface NoteFeignClient {

    /**
     * 设置笔记为草稿状态
     * @param noteId 笔记ID
     * @return 结果
     */
    @PutMapping("/note/{noteId}/to-draft")
    Result<Void> setNoteToDraft(@PathVariable("noteId") Long noteId);
}
