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

    @Select("SELECT * FROM creator_summary ORDER BY total_income DESC LIMIT #{limit}")
    List<CreatorSummary> selectTopCreatorsByRevenue(@Param("limit") Integer limit);

    @Select("SELECT * FROM creator_summary ORDER BY total_followers DESC LIMIT #{limit}")
    List<CreatorSummary> selectTopCreatorsByFollowers(@Param("limit") Integer limit);

    @Select("SELECT * FROM creator_summary WHERE weekly_ranking <= #{limit} ORDER BY weekly_ranking")
    List<CreatorSummary> selectWeeklyTopCreators(@Param("limit") Integer limit);

    @Update("UPDATE creator_summary SET avg_heat_score = #{heatScore}, updated_at = NOW() WHERE creator_id = #{creatorId}")
    int updateHeatScore(@Param("creatorId") Long creatorId, @Param("heatScore") BigDecimal heatScore);

    @Update("UPDATE creator_summary SET avg_rating = #{avgRating}, total_ratings = #{totalRatings}, updated_at = NOW() WHERE creator_id = #{creatorId}")
    int updateRatingStats(@Param("creatorId") Long creatorId, @Param("avgRating") Double avgRating, @Param("totalRatings") Long totalRatings);

    @Select("SELECT * FROM creator_summary")
    List<CreatorSummary> selectAll();

    @Update("UPDATE creator_summary SET " +
            "total_notes = #{totalNotes}, " +
            "total_views = #{totalViews}, " +
            "total_comments = #{totalComments}, " +
            "total_favorites = #{totalFavorites}, " +
            "total_shares = #{totalShares}, " +
            "avg_rating = #{avgRating}, " +
            "total_ratings = #{totalRatings}, " +
            "total_subscribers = #{totalSubscribers}, " +
            "today_income = #{todayIncome}, " +
            "week_income = #{weekIncome}, " +
            "month_income = #{monthIncome}, " +
            "year_income = #{yearIncome}, " +
            "total_income = #{totalIncome}, " +
            "updated_at = NOW() " +
            "WHERE creator_id = #{creatorId}")
    int updateCreatorSummary(
            @Param("creatorId") Long creatorId,
            @Param("totalNotes") Long totalNotes,
            @Param("totalViews") Long totalViews,
            @Param("totalComments") Long totalComments,
            @Param("totalFavorites") Long totalFavorites,
            @Param("totalShares") Long totalShares,
            @Param("avgRating") Double avgRating,
            @Param("totalRatings") Long totalRatings,
            @Param("totalSubscribers") Long totalSubscribers,
            @Param("todayIncome") BigDecimal todayIncome,
            @Param("weekIncome") BigDecimal weekIncome,
            @Param("monthIncome") BigDecimal monthIncome,
            @Param("yearIncome") BigDecimal yearIncome,
            @Param("totalIncome") BigDecimal totalIncome
    );

    @Update("UPDATE creator_summary SET " +
            "today_income = #{todayIncome}, " +
            "week_income = #{weekIncome}, " +
            "month_income = #{monthIncome}, " +
            "year_income = #{yearIncome}, " +
            "total_income = #{totalIncome}, " +
            "last_calculated_at = NOW(), " +
            "updated_at = NOW() " +
            "WHERE creator_id = #{creatorId}")
    int updateIncomeStats(
            @Param("creatorId") Long creatorId,
            @Param("todayIncome") BigDecimal todayIncome,
            @Param("weekIncome") BigDecimal weekIncome,
            @Param("monthIncome") BigDecimal monthIncome,
            @Param("yearIncome") BigDecimal yearIncome,
            @Param("totalIncome") BigDecimal totalIncome
    );
}
