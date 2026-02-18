package com.onlystudents.rating.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹DTO
 */
@Data

public class FavoriteFolderDTO {
    
    private Long id;
    
    private Long userId;
    
    private String name;
    
    private String description;
    
    private Integer count;
    
    private Integer isDefault;
    
    private Integer sortOrder;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
