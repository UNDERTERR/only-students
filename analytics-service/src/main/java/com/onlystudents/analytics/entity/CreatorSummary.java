package com.onlystudents.analytics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("creator_summary")
public class CreatorSummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    private Long totalNotes;

    private Long totalViews;

    private Long totalComments;

    private Long totalFavorites;

    private Long totalShares;

    private Long totalRatings;

    private Double avgRating;

    private Long totalSubscribers;

    private BigDecimal todayIncome;

    private BigDecimal weekIncome;

    private BigDecimal monthIncome;

    private BigDecimal yearIncome;

    private BigDecimal totalIncome;

    private BigDecimal avgHeatScore;

    private Integer weeklyRanking;

    private Integer monthlyRanking;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
