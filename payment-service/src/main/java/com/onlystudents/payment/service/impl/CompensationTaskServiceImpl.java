package com.onlystudents.payment.service.impl;

import com.alibaba.fastjson2.JSON;
import com.onlystudents.payment.entity.CompensationTask;
import com.onlystudents.payment.mapper.CompensationTaskMapper;
import com.onlystudents.payment.service.CompensationTaskService;
import com.onlystudents.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 补偿任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationTaskServiceImpl implements CompensationTaskService {
    
    private final CompensationTaskMapper taskMapper;
    private final PaymentService paymentService;
    
    private static final String TASK_TYPE_INCOME = "INCOME_ALLOCATION";
    private static final int MAX_RETRY_COUNT = 5;
    
    @Override
    @Transactional
    public void createIncomeCompensationTask(Long orderId, Long creatorId, BigDecimal amount) {
        // 检查是否已存在
        CompensationTask existTask = taskMapper.selectByBusinessIdAndType(orderId, TASK_TYPE_INCOME);
        if (existTask != null) {
            log.warn("补偿任务已存在: orderId={}, taskId={}", orderId, existTask.getId());
            return;
        }
        
        CompensationTask task = new CompensationTask();
        task.setTaskType(TASK_TYPE_INCOME);
        task.setBusinessId(orderId);
        task.setStatus(0); // 待处理
        task.setRetryCount(0);
        task.setMaxRetryCount(MAX_RETRY_COUNT);
        
        // 构建参数
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("creatorId", creatorId);
        params.put("amount", amount);
        task.setParams(JSON.toJSONString(params));
        
        // 立即执行一次
        task.setNextExecuteTime(LocalDateTime.now());
        
        taskMapper.insert(task);
        log.info("创建收入分配补偿任务: orderId={}, creatorId={}, amount={}", orderId, creatorId, amount);
    }
    
    @Override
    @Transactional
    public boolean executeCompensation(CompensationTask task) {
        log.info("执行补偿任务: taskId={}, type={}, retry={}", 
                task.getId(), task.getTaskType(), task.getRetryCount());
        
        try {
            if (TASK_TYPE_INCOME.equals(task.getTaskType())) {
                return executeIncomeCompensation(task);
            } else {
                log.error("未知的补偿任务类型: {}", task.getTaskType());
                return false;
            }
        } catch (Exception e) {
            log.error("补偿任务执行失败: taskId={}", task.getId(), e);
            return false;
        }
    }
    
    /**
     * 执行收入分配补偿
     */
    private boolean executeIncomeCompensation(CompensationTask task) {
        try {
            Map<String, Object> params = JSON.parseObject(task.getParams(), Map.class);
            Long orderId = Long.valueOf(params.get("orderId").toString());
            Long creatorId = Long.valueOf(params.get("creatorId").toString());
            BigDecimal amount = new BigDecimal(params.get("amount").toString());
            
            // 调用支付服务增加收入
            paymentService.addIncome(creatorId, orderId, amount);
            
            // 标记成功
            taskMapper.markSuccess(task.getId());
            log.info("收入分配补偿成功: taskId={}, orderId={}, creatorId={}", 
                    task.getId(), orderId, creatorId);
            return true;
            
        } catch (Exception e) {
            log.error("收入分配补偿失败: taskId={}", task.getId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public void processPendingTasks() {
        // 每次处理20个任务
        List<CompensationTask> tasks = taskMapper.selectPendingTasks(20);
        if (tasks.isEmpty()) {
            return;
        }
        
        log.info("发现 {} 个待处理补偿任务", tasks.size());
        
        for (CompensationTask task : tasks) {
            try {
                // 尝试标记为处理中（乐观锁）
                int rows = taskMapper.markProcessing(task.getId());
                if (rows == 0) {
                    continue; // 已被其他线程处理
                }
                
                // 执行补偿
                boolean success = executeCompensation(task);
                
                if (!success) {
                    handleFailedTask(task);
                }
                
            } catch (Exception e) {
                log.error("处理补偿任务异常: taskId={}", task.getId(), e);
                handleFailedTask(task);
            }
        }
    }
    
    @Override
    @Transactional
    public void processTimeoutTasks() {
        // 处理超时的任务（状态为处理中但超过5分钟）
        List<CompensationTask> tasks = taskMapper.selectTimeoutTasks(10);
        if (tasks.isEmpty()) {
            return;
        }
        
        log.info("发现 {} 个超时补偿任务", tasks.size());
        
        for (CompensationTask task : tasks) {
            try {
                log.warn("补偿任务超时，重新放入队列: taskId={}", task.getId());
                
                // 重置为待处理状态
                LocalDateTime nextTime = calculateNextExecuteTime(task.getRetryCount());
                taskMapper.markFailed(task.getId(), nextTime, "任务超时");
                
            } catch (Exception e) {
                log.error("处理超时任务异常: taskId={}", task.getId(), e);
            }
        }
    }
    
    /**
     * 处理失败任务
     */
    private void handleFailedTask(CompensationTask task) {
        int newRetryCount = task.getRetryCount() + 1;
        
        if (newRetryCount >= task.getMaxRetryCount()) {
            // 超过最大重试次数，放弃
            taskMapper.markAbandoned(task.getId(), "超过最大重试次数: " + newRetryCount);
            log.error("补偿任务放弃: taskId={}, retryCount={}", task.getId(), newRetryCount);
        } else {
            // 增加重试次数，设置下次执行时间
            LocalDateTime nextTime = calculateNextExecuteTime(newRetryCount);
            taskMapper.markFailed(task.getId(), nextTime, "执行失败，等待重试");
            taskMapper.incrementRetryCount(task.getId());
            log.warn("补偿任务失败，等待重试: taskId={}, retryCount={}", task.getId(), newRetryCount);
        }
    }
    
    /**
     * 计算下次执行时间（指数退避）
     */
    private LocalDateTime calculateNextExecuteTime(int retryCount) {
        // 退避策略：1分钟、2分钟、4分钟、8分钟、16分钟...
        int minutes = (int) Math.pow(2, retryCount);
        return LocalDateTime.now().plusMinutes(minutes);
    }
    
    @Override
    public Long checkAlertCondition() {
        Long failedCount = taskMapper.countFailedTasks();
        if (failedCount > 0) {
            log.warn("发现 {} 个失败补偿任务，需要人工介入", failedCount);
        }
        return failedCount;
    }
}
