package com.onlystudents.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    private String targetTitle;

    private Long contentId;

    private Integer contentType;

    private String content;

    @NotBlank(message = "审核原因不能为空")
    private String reason;

    @NotNull(message = "审核动作不能为空")
    private Integer action;
}
