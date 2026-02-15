package com.onlystudents.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    
    // 笔记相关信息
    @Schema(description = "笔记标题")
    private String title;
    
    @Schema(description = "笔记封面图")
    private String coverImage;
    
    @Schema(description = "作者昵称")
    private String authorNickname;
    
    @Schema(description = "作者头像")
    private String authorAvatar;
    
    @Schema(description = "观看数")
    private Integer viewCount;
    
    @Schema(description = "收藏数")
    private Integer favoriteCount;
    
    @Schema(description = "评论数")
    private Integer commentCount;
    
    @Schema(description = "平均评分")
    private BigDecimal averageRating;
    
    @Schema(description = "评分数量")
    private Integer ratingCount;
    
    @Schema(description = "标签")
    private List<String> tags;
}
