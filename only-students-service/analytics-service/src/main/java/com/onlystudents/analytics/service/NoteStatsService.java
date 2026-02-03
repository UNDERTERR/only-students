package com.onlystudents.analytics.service;

import com.onlystudents.analytics.entity.NoteStats;

import java.math.BigDecimal;
import java.util.List;

public interface NoteStatsService {

    NoteStats getNoteStats(Long noteId);

    List<NoteStats> getNotesByCreatorOrderByHeat(Long creatorId);

    List<NoteStats> getTopNotes(Integer limit);

    void updateHeatScore(Long noteId, BigDecimal heatScore, Integer ranking);
}
