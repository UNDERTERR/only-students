package com.onlystudents.admin.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long totalUsers;
    private Long todayNewUsers;
    private Long weekNewUsers;
    private Long monthNewUsers;
    private Long totalCreators;
    private Long todayNewCreators;
}
