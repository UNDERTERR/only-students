package com.onlystudents.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.analytics.entity.NoteStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface NoteStatsMapper extends BaseMapper<NoteStats> {

    @Select("SELECT * FROM note_stats WHERE note_id = #{noteId}")
    NoteStats selectByNoteId(@Param("noteId") Long noteId);

    @Select("SELECT * FROM note_stats WHERE creator_id = #{creatorId} ORDER BY hot_score DESC")
    List<NoteStats> selectByCreatorIdOrderByHeat(@Param("creatorId") Long creatorId);

    @Select("SELECT * FROM note_stats WHERE ranking <= #{limit} ORDER BY ranking")
    List<NoteStats> selectTopNotes(@Param("limit") Integer limit);

    @Update("UPDATE note_stats SET hot_score = #{heatScore}, ranking = #{ranking}, updated_at = NOW() WHERE note_id = #{noteId}")
    int updateHeatScoreAndRanking(@Param("noteId") Long noteId, @Param("heatScore") BigDecimal heatScore, @Param("ranking") Integer ranking);
}
