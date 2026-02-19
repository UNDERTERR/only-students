package com.onlystudents.subscription.dto;

import lombok.Data;

@Data
public class CreatorConfigDTO {
    
    private Long id;
    
    private Long creatorId;
    
    private Integer isEnabled;
}
