package com.onlystudents.analytics.service.impl;

import com.onlystudents.analytics.entity.CreatorSummary;
import com.onlystudents.analytics.mapper.CreatorSummaryMapper;
import com.onlystudents.analytics.service.CreatorSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreatorSummaryServiceImpl implements CreatorSummaryService {

    private final CreatorSummaryMapper creatorSummaryMapper;

    @Override
    public CreatorSummary getCreatorSummary(Long creatorId) {
        return creatorSummaryMapper.selectByCreatorId(creatorId);
    }

    @Override
    public List<CreatorSummary> getTopCreatorsByRevenue(Integer limit) {
        return creatorSummaryMapper.selectTopCreatorsByRevenue(limit);
    }

    @Override
    public List<CreatorSummary> getTopCreatorsByFollowers(Integer limit) {
        return creatorSummaryMapper.selectTopCreatorsByFollowers(limit);
    }

    @Override
    public List<CreatorSummary> getWeeklyTopCreators(Integer limit) {
        return creatorSummaryMapper.selectWeeklyTopCreators(limit);
    }

    @Override
    @Transactional
    public void updateHeatScore(Long creatorId, BigDecimal heatScore) {
        creatorSummaryMapper.updateHeatScore(creatorId, heatScore);
    }
}
