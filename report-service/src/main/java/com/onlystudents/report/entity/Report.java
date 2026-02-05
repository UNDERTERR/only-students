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
    
    private Integer reason;
    
    private String description;
    
    private String evidence;
    
    private Integer status;
    
    private Long handlerId;
    
    private String handleResult;
    
    private LocalDateTime handleTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
