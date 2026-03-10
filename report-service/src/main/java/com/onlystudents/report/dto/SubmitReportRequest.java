package com.onlystudents.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "提交举报请求")
public class SubmitReportRequest {
    
    @Schema(description = "目标类型: 1-笔记 2-评论 3-用户")
    private Integer targetType;
    
    @Schema(description = "目标ID")
    private Long targetId;
    
    @Schema(description = "举报原因: 内容低俗/虚假信息/抄袭侵权/恶意营销/人身攻击/违法违规/其他")
    private String reason;
    
    @Schema(description = "详细描述", required = false)
    private String description;
    
    @Schema(description = "证据截图URL", required = false)
    private String evidence;
}
