package com.onlystudents.analytics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_stats")
public class DailyStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    @TableField("stat_date")
    private LocalDate statsDate;

    private Integer newViews;

    private Integer newFavorites;

    private Integer newComments;

    private Integer newShares;

    private Integer newSubscribers;

    private Integer lostSubscribers;

    private Integer orderCount;

    private BigDecimal orderAmount;

    private BigDecimal incomeAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
