package com.onlystudents.analytics.service;

import com.onlystudents.analytics.entity.CreatorSummary;
import com.onlystudents.analytics.entity.DailyStats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DailyStatsService {

    DailyStats getDailyStats(Long creatorId, LocalDate date);

    List<DailyStats> getDailyStatsRange(Long creatorId, LocalDate startDate, LocalDate endDate);

    void recordDailyStats(DailyStats dailyStats);

    java.util.Map<String, Object> getSummaryStats(Long creatorId, LocalDate startDate, LocalDate endDate);
}
