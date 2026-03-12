package com.onlystudents.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.analytics.entity.DailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DailyStatsMapper extends BaseMapper<DailyStats> {

    @Select("SELECT * FROM daily_stats WHERE creator_id = #{creatorId} AND stat_date = #{date}")
    DailyStats selectByCreatorAndDate(@Param("creatorId") Long creatorId, @Param("date") LocalDate date);

    @Select("SELECT * FROM daily_stats WHERE creator_id = #{creatorId} AND stat_date BETWEEN #{startDate} AND #{endDate} ORDER BY stat_date DESC")
    List<DailyStats> selectByCreatorAndDateRange(@Param("creatorId") Long creatorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT creator_id, SUM(new_views) as new_views, SUM(new_comments) as new_comments, " +
            "SUM(new_favorites) as new_favorites, SUM(new_shares) as new_shares, " +
            "SUM(new_subscribers) as new_subscribers, SUM(income_amount) as income_amount " +
            "FROM daily_stats WHERE stat_date = #{date} GROUP BY creator_id")
    List<Map<String, Object>> selectYesterdayStats(@Param("date") LocalDate date);

    @Select("SELECT SUM(new_views) as total_views, SUM(new_comments) as total_comments, " +
            "SUM(new_favorites) as total_favorites, SUM(income_amount) as total_income " +
            "FROM daily_stats WHERE creator_id = #{creatorId} AND stat_date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> selectSummaryByDateRange(@Param("creatorId") Long creatorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
