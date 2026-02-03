package com.onlystudents.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_record")
public class AuditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long targetId;

    private Integer targetType;

    private String targetTitle;

    private Long contentId;

    private Integer contentType;

    private String content;

    private String reason;

    private Integer action;

    private Long operatorId;

    private String operatorName;

    private Integer status;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
