package com.onlystudents.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送消息请求")
public class SendMessageRequest {
    
    @Schema(description = "接收者ID")
    private Long receiverId;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "消息类型: 1-文本 2-图片 3-文件", required = false)
    private Integer type = 1;
}
