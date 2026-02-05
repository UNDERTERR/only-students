package com.onlystudents.analytics.service.impl;

import com.onlystudents.analytics.entity.HourlyStats;
import com.onlystudents.analytics.mapper.HourlyStatsMapper;
import com.onlystudents.analytics.service.HourlyStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HourlyStatsServiceImpl implements HourlyStatsService {

    private final HourlyStatsMapper hourlyStatsMapper;

    @Override
    public List<HourlyStats> getHourlyStatsRange(Long creatorId, LocalDateTime startTime, LocalDateTime endTime) {
        return hourlyStatsMapper.selectByCreatorAndTimeRange(creatorId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getPeakHours(Long creatorId, LocalDateTime startTime) {
        return hourlyStatsMapper.selectPeakHoursByCreator(creatorId, startTime);
    }

    @Override
    @Transactional
    public void recordHourlyStats(HourlyStats hourlyStats) {
        hourlyStatsMapper.insert(hourlyStats);
    }
}
