package com.onlystudents.analytics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("note_stats")
public class NoteStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long creatorId;

    private Long totalViews;

    private Long totalFavorites;

    private Long totalComments;

    private Long totalShares;

    private Integer avgReadingTime;

    private BigDecimal completionRate;

    private BigDecimal heatScore;

    private LocalDateTime lastCalculatedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
