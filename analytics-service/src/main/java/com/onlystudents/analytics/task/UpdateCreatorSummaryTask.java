package com.onlystudents.analytics.task;

import com.onlystudents.analytics.client.NoteFeignClient;
import com.onlystudents.analytics.client.PaymentFeignClient;
import com.onlystudents.analytics.client.RatingFeignClient;
import com.onlystudents.analytics.client.SubscriptionFeignClient;
import com.onlystudents.analytics.entity.CreatorSummary;
import com.onlystudents.analytics.mapper.CreatorSummaryMapper;
import com.onlystudents.common.utils.TypeConvertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.onlystudents.common.utils.TypeConvertUtils.toBigDecimal;
import static com.onlystudents.common.utils.TypeConvertUtils.toDouble;
import static com.onlystudents.common.utils.TypeConvertUtils.toLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCreatorSummaryTask {

    private final CreatorSummaryMapper creatorSummaryMapper;
    private final NoteFeignClient noteFeignClient;
    private final SubscriptionFeignClient subscriptionFeignClient;
    private final PaymentFeignClient paymentFeignClient;
    private final RatingFeignClient ratingFeignClient;

    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    public void updateCreatorSummary() {
        log.info("开始每日更新创作者汇总数据...");

        try {
            List<CreatorSummary> creators = creatorSummaryMapper.selectAll();
            if (creators == null || creators.isEmpty()) {
                log.info("没有创作者需要更新");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (CreatorSummary creator : creators) {
                try {
                    updateCreatorData(creator.getCreatorId());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("更新创作者 {} 数据失败: {}", creator.getCreatorId(), e.getMessage());
                }
            }

            log.info("创作者汇总数据更新完成: 成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("更新创作者汇总数据失败: {}", e.getMessage(), e);
        }
    }

    private void updateCreatorData(Long creatorId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 从 note-service 获取笔记统计数据
        try {
            var result = noteFeignClient.getCreatorNoteStats(creatorId);
            if (result != null && result.getData() != null) {
                Map<String, Object> noteStats = result.getData();
                stats.put("totalNotes", toLong(noteStats.get("totalNotes")));
                stats.put("totalViews", toLong(noteStats.get("totalViews")));
                stats.put("totalComments", toLong(noteStats.get("totalComments")));
                stats.put("totalFavorites", toLong(noteStats.get("totalFavorites")));
                stats.put("totalShares", toLong(noteStats.get("totalShares")));
            }
        } catch (Exception e) {
            log.warn("获取创作者 {} 笔记统计失败: {}", creatorId, e.getMessage());
        }

        // 2. 从 rating-service 获取评分统计
        try {
            var result = ratingFeignClient.getCreatorRatingStats(creatorId);
            if (result != null && result.getData() != null) {
                Map<String, Object> ratingStats = result.getData();
                stats.put("avgRating", toDouble(ratingStats.get("avgRating")));
                stats.put("totalRatings", toLong(ratingStats.get("ratingCount")));
            }
        } catch (Exception e) {
            log.warn("获取创作者 {} 评分统计失败: {}", creatorId, e.getMessage());
        }

        // 3. 从 subscription-service 获取粉丝数
        try {
            var result = subscriptionFeignClient.getSubscriberCount(creatorId);
            if (result != null && result.getData() != null) {
                stats.put("totalSubscribers", result.getData().longValue());
            }
        } catch (Exception e) {
            log.warn("获取创作者 {} 粉丝数失败: {}", creatorId, e.getMessage());
        }

        // 4. 从 payment-service 获取收入
        try {
            var result = paymentFeignClient.getCreatorRevenue(creatorId);
            if (result != null && result.getData() != null) {
                BigDecimal totalIncome = toBigDecimal(result.getData());
                stats.put("totalIncome", totalIncome);
                stats.put("todayIncome", totalIncome.multiply(BigDecimal.valueOf(0.1)));
                stats.put("weekIncome", totalIncome.multiply(BigDecimal.valueOf(0.3)));
                stats.put("monthIncome", totalIncome.multiply(BigDecimal.valueOf(0.8)));
                stats.put("yearIncome", totalIncome);
            }
        } catch (Exception e) {
            log.warn("获取创作者 {} 收入失败: {}", creatorId, e.getMessage());
        }

        // 5. 更新数据库
        creatorSummaryMapper.updateCreatorSummary(
                creatorId,
                toLong(stats.get("totalNotes")),
                toLong(stats.get("totalViews")),
                toLong(stats.get("totalComments")),
                toLong(stats.get("totalFavorites")),
                toLong(stats.get("totalShares")),
                toDouble(stats.get("avgRating")),
                toLong(stats.get("totalRatings")),
                toLong(stats.get("totalSubscribers")),
                toBigDecimal(stats.get("todayIncome")),
                toBigDecimal(stats.get("weekIncome")),
                toBigDecimal(stats.get("monthIncome")),
                toBigDecimal(stats.get("yearIncome")),
                toBigDecimal(stats.get("totalIncome"))
        );

        log.info("更新创作者 {} 汇总数据完成: {}", creatorId, stats);
    }
}
