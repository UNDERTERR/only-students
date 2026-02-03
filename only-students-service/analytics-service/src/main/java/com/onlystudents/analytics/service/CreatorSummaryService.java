package com.onlystudents.analytics.service;

import com.onlystudents.analytics.entity.CreatorSummary;

import java.math.BigDecimal;
import java.util.List;

public interface CreatorSummaryService {

    CreatorSummary getCreatorSummary(Long creatorId);

    List<CreatorSummary> getTopCreatorsByRevenue(Integer limit);

    List<CreatorSummary> getTopCreatorsByFollowers(Integer limit);

    List<CreatorSummary> getWeeklyTopCreators(Integer limit);

    void updateHeatScore(Long creatorId, BigDecimal heatScore);
}
