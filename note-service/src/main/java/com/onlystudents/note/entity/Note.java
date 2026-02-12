package com.onlystudents.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    // 作者信息（冗余存储，用于ES搜索）
    private String authorUsername;

    private String authorNickname;

    private String authorAvatar;

    private String title;

    private String content;

    private String coverImage;

    private Long categoryId;

    private Integer visibility;

    private BigDecimal price;

    private Long originalFileId;

    private Long pdfFileId;

    private Integer status;

    @TableLogic
    private Integer deleted;

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

    @TableField(typeHandler =JacksonTypeHandler.class)
    private List<String> tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
