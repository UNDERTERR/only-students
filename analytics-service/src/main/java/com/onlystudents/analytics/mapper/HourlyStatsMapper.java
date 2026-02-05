package com.onlystudents.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.analytics.entity.HourlyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HourlyStatsMapper extends BaseMapper<HourlyStats> {

    @Select("SELECT * FROM hourly_stats WHERE creator_id = #{creatorId} AND stats_time BETWEEN #{startTime} AND #{endTime} ORDER BY stats_time")
    List<HourlyStats> selectByCreatorAndTimeRange(@Param("creatorId") Long creatorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT hour_of_day, SUM(view_count) as total_views FROM hourly_stats WHERE creator_id = #{creatorId} AND stats_time >= #{startTime} GROUP BY hour_of_day ORDER BY total_views DESC")
    List<java.util.Map<String, Object>> selectPeakHoursByCreator(@Param("creatorId") Long creatorId, @Param("startTime") LocalDateTime startTime);
}
