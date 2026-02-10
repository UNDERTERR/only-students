package com.onlystudents.search.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记搜索文档实体
 * 对应 Elasticsearch 中的索引
 */
@Data
public class NoteDocument {
    
    private Long noteId;
    private String title;
    private String content;
    private List<String> tags;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String username;      // 作者用户名
    private String nickname;      // 作者昵称  
    private String userAvatar;    // 作者头像
    private Integer educationLevel;
    private Long schoolId;
    private String schoolName;
    private String subject;
    private Integer visibility;
    private BigDecimal price;
    private Integer status;
    private Double hotScore;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Double rating;
    private Integer ratingCount;
    private String coverImage;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
