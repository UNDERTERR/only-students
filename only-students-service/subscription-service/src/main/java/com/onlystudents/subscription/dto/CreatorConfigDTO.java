package com.onlystudents.subscription.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatorConfigDTO {
    
    private Long id;
    
    private Long creatorId;
    
    private BigDecimal price;
    
    private Integer isEnabled;
    
    private String benefits;
    
    private String description;
}
