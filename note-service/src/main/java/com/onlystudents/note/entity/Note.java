package com.onlystudents.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String authorNickname;

    private String authorAvatar;

    private String title;

    private String content;

    private String coverImage;

    private Integer visibility;

    private BigDecimal price;

    private String attachments;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private Integer viewCount;
    
    private Integer ratingCount;
    
    private BigDecimal averageRating;
    
    private Integer favoriteCount;

    private Integer commentCount;

    private Integer shareCount;

    private Double hotScore;

    private Integer educationLevel;

    private Long schoolId;

    private String schoolName;

    private LocalDateTime publishTime;

    @TableField(exist = false)
    private List<String> tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
