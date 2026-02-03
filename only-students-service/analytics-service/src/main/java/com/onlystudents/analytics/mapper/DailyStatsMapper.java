package com.onlystudents.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.analytics.entity.DailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyStatsMapper extends BaseMapper<DailyStats> {

    @Select("SELECT * FROM daily_stats WHERE creator_id = #{creatorId} AND stats_date = #{date}")
    DailyStats selectByCreatorAndDate(@Param("creatorId") Long creatorId, @Param("date") LocalDate date);

    @Select("SELECT * FROM daily_stats WHERE creator_id = #{creatorId} AND stats_date BETWEEN #{startDate} AND #{endDate} ORDER BY stats_date DESC")
    List<DailyStats> selectByCreatorAndDateRange(@Param("creatorId") Long creatorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT SUM(view_count) as total_views, SUM(like_count) as total_likes, SUM(comment_count) as total_comments, " +
            "SUM(collect_count) as total_collects, SUM(revenue) as total_revenue " +
            "FROM daily_stats WHERE creator_id = #{creatorId} AND stats_date BETWEEN #{startDate} AND #{endDate}")
    java.util.Map<String, Object> selectSummaryByDateRange(@Param("creatorId") Long creatorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
