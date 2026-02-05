package com.onlystudents.payment.service;

import com.onlystudents.payment.entity.CompensationTask;

import java.math.BigDecimal;

/**
 * 补偿任务服务
 */
public interface CompensationTaskService {
    
    /**
     * 创建收入分配补偿任务
     * 
     * @param orderId 订单ID
     * @param creatorId 创作者ID
     * @param amount 金额
     */
    void createIncomeCompensationTask(Long orderId, Long creatorId, BigDecimal amount);
    
    /**
     * 执行补偿任务
     * 
     * @param task 补偿任务
     * @return 是否成功
     */
    boolean executeCompensation(CompensationTask task);
    
    /**
     * 处理待补偿任务
     */
    void processPendingTasks();
    
    /**
     * 处理超时任务
     */
    void processTimeoutTasks();
    
    /**
     * 检查是否需要告警
     * 
     * @return 失败任务数
     */
    Long checkAlertCondition();
}
