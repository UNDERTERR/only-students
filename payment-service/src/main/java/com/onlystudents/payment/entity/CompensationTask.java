package com.onlystudents.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 补偿任务表
 * 记录需要补偿的业务操作
 */
@Data
@TableName("compensation_task")
public class CompensationTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务类型：INCOME_ALLOCATION-收入分配
     */
    private String taskType;
    
    /**
     * 业务ID（如订单ID）
     */
    private Long businessId;
    
    /**
     * 任务状态：0-待处理 1-处理中 2-成功 3-失败 4-放弃
     */
    private Integer status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 任务参数（JSON格式）
     */
    private String params;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;
    
    /**
     * 成功时间
     */
    private LocalDateTime successTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
