package com.onlystudents.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记分享DTO
 */
@Data
@Schema(description = "笔记分享信息")
public class NoteShareDTO {
    
    @Schema(description = "分享ID")
    private Long id;
    
    @Schema(description = "笔记ID")
    private Long noteId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "分享类型(1-微信 2-QQ 3-链接)")
    private Integer shareType;
    
    @Schema(description = "分享码")
    private String shareCode;
    
    @Schema(description = "点击次数")
    private Integer clickCount;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
