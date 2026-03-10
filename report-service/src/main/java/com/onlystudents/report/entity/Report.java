package com.onlystudents.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reporterId;
    
    private Long targetId;
    
    private Integer targetType;
    
    @TableField("target_user_id")
    private Long targetUserId;
    
    @TableField("reason_type")
    private String reasonType;
    
    @TableField("reason_detail")
    private String description;
    
    @TableField("evidence_urls")
    private String evidence;
    
    private Integer status;
    
    @TableField("processor_id")
    private Long handlerId;
    
    @TableField("process_result")
    private String handleResult;
    
    @TableField("process_time")
    private LocalDateTime handleTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
