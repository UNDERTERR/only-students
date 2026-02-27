package com.onlystudents.search.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记搜索文档实体
 * 对应 Elasticsearch 中的索引
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoteDocument {
    
    private Long noteId;
    private String title;
    private String content;
    private List<String> tags;
    private Long userId;
    private String authorNickname;  // 作者昵称  
    private String authorAvatar;    // 作者头像
    private Integer educationLevel;
    private Long schoolId;
    private String schoolName;
    private Integer visibility;
    private BigDecimal price;
    private Integer status;
    private Integer deleted;
    private Double hotScore;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Double rating;
    private Integer ratingCount;
    private String coverImage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
