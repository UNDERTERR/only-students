package com.onlystudents.analytics.service;

import com.onlystudents.analytics.entity.HourlyStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HourlyStatsService {

    List<HourlyStats> getHourlyStatsRange(Long creatorId, LocalDateTime startTime, LocalDateTime endTime);

    List<java.util.Map<String, Object>> getPeakHours(Long creatorId, LocalDateTime startTime);

    void recordHourlyStats(HourlyStats hourlyStats);
}
