package com.onlystudents.report.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 评论服务 Feign 客户端
 * 用于 report-service 调用 comment-service 删除评论
 */
@FeignClient(name = "comment-service")
public interface CommentFeignClient {

    /**
     * 物理删除评论
     * @param commentId 评论ID
     * @return 结果
     */
    @DeleteMapping("/comment/{commentId}")
    Result<Void> deleteComment(@PathVariable("commentId") Long commentId);
}
