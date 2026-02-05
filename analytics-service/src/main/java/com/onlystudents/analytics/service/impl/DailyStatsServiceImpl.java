package com.onlystudents.analytics.service.impl;

import com.onlystudents.analytics.entity.DailyStats;
import com.onlystudents.analytics.mapper.DailyStatsMapper;
import com.onlystudents.analytics.service.DailyStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyStatsServiceImpl implements DailyStatsService {

    private final DailyStatsMapper dailyStatsMapper;

    @Override
    public DailyStats getDailyStats(Long creatorId, LocalDate date) {
        return dailyStatsMapper.selectByCreatorAndDate(creatorId, date);
    }

    @Override
    public List<DailyStats> getDailyStatsRange(Long creatorId, LocalDate startDate, LocalDate endDate) {
        return dailyStatsMapper.selectByCreatorAndDateRange(creatorId, startDate, endDate);
    }

    @Override
    @Transactional
    public void recordDailyStats(DailyStats dailyStats) {
        DailyStats existing = dailyStatsMapper.selectByCreatorAndDate(dailyStats.getCreatorId(), dailyStats.getStatsDate());
        if (existing != null) {
            dailyStats.setId(existing.getId());
            dailyStatsMapper.updateById(dailyStats);
        } else {
            dailyStatsMapper.insert(dailyStats);
        }
    }

    @Override
    public Map<String, Object> getSummaryStats(Long creatorId, LocalDate startDate, LocalDate endDate) {
        return dailyStatsMapper.selectSummaryByDateRange(creatorId, startDate, endDate);
    }
}
