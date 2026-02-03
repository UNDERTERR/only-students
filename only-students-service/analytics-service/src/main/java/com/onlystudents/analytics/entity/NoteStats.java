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

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Long collectCount;

    private Long shareCount;

    private Long downloadCount;

    private BigDecimal heatScore;

    private Integer ranking;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
