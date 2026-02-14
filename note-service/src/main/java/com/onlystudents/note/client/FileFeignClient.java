package com.onlystudents.note.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 文件服务Feign客户端
 */
@FeignClient(name = "file-service", path = "/file")
public interface FileFeignClient {
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    Result<Void> deleteFile(@PathVariable("fileId") Long fileId, 
                           @RequestHeader("X-User-Id") Long userId);
}
