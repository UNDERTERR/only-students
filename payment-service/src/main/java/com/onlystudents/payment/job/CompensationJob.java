package com.onlystudents.payment.job;

import com.onlystudents.payment.service.CompensationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 补偿任务定时调度器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationJob {
    
    private final CompensationTaskService compensationTaskService;
    
    /**
     * 处理待补偿任务
     * 每30秒执行一次
     */
    @Scheduled(fixedDelay = 30000)
    public void processPendingTasks() {
        try {
            log.debug("开始执行补偿任务调度...");
            compensationTaskService.processPendingTasks();
        } catch (Exception e) {
            log.error("补偿任务调度异常", e);
        }
    }
    
    /**
     * 处理超时任务
     * 每2分钟执行一次
     */
    @Scheduled(fixedDelay = 120000)
    public void processTimeoutTasks() {
        try {
            log.debug("开始检查超时任务...");
            compensationTaskService.processTimeoutTasks();
        } catch (Exception e) {
            log.error("处理超时任务异常", e);
        }
    }
    
    /**
     * 监控告警检查
     * 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 300000)
    public void checkAlert() {
        try {
            Long failedCount = compensationTaskService.checkAlertCondition();
            if (failedCount > 0) {
                // 这里可以集成钉钉/飞书/邮件告警
                log.error("【告警】补偿任务异常，失败任务数: {}", failedCount);
            }
        } catch (Exception e) {
            log.error("告警检查异常", e);
        }
    }
}
