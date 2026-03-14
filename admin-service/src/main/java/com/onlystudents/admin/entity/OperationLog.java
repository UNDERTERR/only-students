package com.onlystudents.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_operation_log")
public class OperationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long adminId;
    
    private String operationType;
    
    private String operationDesc;
    
    private String requestMethod;
    
    private String requestUrl;
    
    private String requestParams;
    
    private String responseData;
    
    private String ip;
    
    private Integer executionTime;
    
    private Integer status;
    
    private String errorMsg;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
