package com.onlystudents.analytics.task;

import com.onlystudents.analytics.mapper.CreatorSummaryMapper;
import com.onlystudents.analytics.mapper.DailyStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.onlystudents.common.utils.TypeConvertUtils.toBigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateDailyStatsTask {

    private final DailyStatsMapper dailyStatsMapper;
    private final CreatorSummaryMapper creatorSummaryMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyStats() {
        log.info("开始生成每日统计数据...");
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        try {
            List<Map<String, Object>> dailySummaries = dailyStatsMapper.selectYesterdayStats(yesterday);
            
            if (dailySummaries == null || dailySummaries.isEmpty()) {
                log.info("没有需要生成的每日统计数据");
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> summary : dailySummaries) {
                try {
                    Long creatorId = ((Number) summary.get("creator_id")).longValue();
                    
                    BigDecimal todayIncome = toBigDecimal(summary.get("income_amount"));
                    
                    Map<String, Object> weekSummary = dailyStatsMapper.selectSummaryByDateRange(
                            creatorId, 
                            LocalDate.now().minusDays(7), 
                            LocalDate.now()
                    );
                    Map<String, Object> monthSummary = dailyStatsMapper.selectSummaryByDateRange(
                            creatorId, 
                            LocalDate.now().minusDays(30), 
                            LocalDate.now()
                    );
                    
                    BigDecimal weekIncome = toBigDecimal(weekSummary != null ? weekSummary.get("total_income") : null);
                    BigDecimal monthIncome = toBigDecimal(monthSummary != null ? monthSummary.get("total_income") : null);
                    
                    creatorSummaryMapper.updateIncomeStats(
                            creatorId,
                            todayIncome,
                            weekIncome,
                            monthIncome,
                            todayIncome.multiply(BigDecimal.valueOf(365)),
                            todayIncome.multiply(BigDecimal.valueOf(365))
                    );
                    
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("更新创作者收入统计失败: {}", e.getMessage());
                }
            }
            
            log.info("每日统计数据生成完成: 成功={}, 失败={}", successCount, failCount);
            
        } catch (Exception e) {
            log.error("生成每日统计数据失败: {}", e.getMessage(), e);
        }
    }
}
