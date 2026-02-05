package com.onlystudents.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记评分DTO
 */
@Data
@Schema(description = "笔记评分信息")
public class NoteRatingDTO {
    
    @Schema(description = "评分ID")
    private Long id;
    
    @Schema(description = "笔记ID")
    private Long noteId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "评分(1-5)")
    private Integer score;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
