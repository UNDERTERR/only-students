package com.onlystudents.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.analytics.entity.CreatorSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CreatorSummaryMapper extends BaseMapper<CreatorSummary> {

    @Select("SELECT * FROM creator_summary WHERE creator_id = #{creatorId}")
    CreatorSummary selectByCreatorId(@Param("creatorId") Long creatorId);

    @Select("SELECT * FROM creator_summary ORDER BY total_revenue DESC LIMIT #{limit}")
    List<CreatorSummary> selectTopCreatorsByRevenue(@Param("limit") Integer limit);

    @Select("SELECT * FROM creator_summary ORDER BY total_followers DESC LIMIT #{limit}")
    List<CreatorSummary> selectTopCreatorsByFollowers(@Param("limit") Integer limit);

    @Select("SELECT * FROM creator_summary WHERE weekly_ranking <= #{limit} ORDER BY weekly_ranking")
    List<CreatorSummary> selectWeeklyTopCreators(@Param("limit") Integer limit);

    @Update("UPDATE creator_summary SET avg_heat_score = #{heatScore}, updated_at = NOW() WHERE creator_id = #{creatorId}")
    int updateHeatScore(@Param("creatorId") Long creatorId, @Param("heatScore") BigDecimal heatScore);
}
