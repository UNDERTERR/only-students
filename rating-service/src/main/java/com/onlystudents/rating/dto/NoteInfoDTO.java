package com.onlystudents.rating.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NoteInfoDTO {
    private Long id;
    private Long userId;
    private String authorNickname;
    private String authorAvatar;
    private String title;
    private String coverImage;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private List<String> tags;
}
