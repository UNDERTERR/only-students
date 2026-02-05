package com.onlystudents.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记收藏DTO
 */
@Data
@Schema(description = "笔记收藏信息")
public class NoteFavoriteDTO {
    
    @Schema(description = "收藏ID")
    private Long id;
    
    @Schema(description = "笔记ID")
    private Long noteId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "收藏夹ID")
    private Long folderId;
    
    @Schema(description = "收藏时间")
    private LocalDateTime createdAt;
}
