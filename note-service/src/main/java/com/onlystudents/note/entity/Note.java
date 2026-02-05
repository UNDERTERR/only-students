package com.onlystudents.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private Integer visibility;
    
    private BigDecimal price;
    
    private Long originalFileId;
    
    private Long pdfFileId;
    
    private Integer status;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer favoriteCount;
    
    private Integer commentCount;
    
    private Integer shareCount;
    
    private Double hotScore;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private String subject;
    
    private LocalDateTime publishTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
