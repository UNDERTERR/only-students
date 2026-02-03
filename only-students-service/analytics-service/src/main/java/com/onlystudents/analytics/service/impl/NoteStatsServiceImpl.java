package com.onlystudents.analytics.service.impl;

import com.onlystudents.analytics.entity.NoteStats;
import com.onlystudents.analytics.mapper.NoteStatsMapper;
import com.onlystudents.analytics.service.NoteStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteStatsServiceImpl implements NoteStatsService {

    private final NoteStatsMapper noteStatsMapper;

    @Override
    public NoteStats getNoteStats(Long noteId) {
        return noteStatsMapper.selectByNoteId(noteId);
    }

    @Override
    public List<NoteStats> getNotesByCreatorOrderByHeat(Long creatorId) {
        return noteStatsMapper.selectByCreatorIdOrderByHeat(creatorId);
    }

    @Override
    public List<NoteStats> getTopNotes(Integer limit) {
        return noteStatsMapper.selectTopNotes(limit);
    }

    @Override
    @Transactional
    public void updateHeatScore(Long noteId, BigDecimal heatScore, Integer ranking) {
        noteStatsMapper.updateHeatScoreAndRanking(noteId, heatScore, ranking);
    }
}
