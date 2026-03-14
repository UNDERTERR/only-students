package com.onlystudents.admin.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class AdminStatsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long totalUsers;
    private Long todayNewUsers;
    private Long weekNewUsers;
    private Long monthNewUsers;
    private Long totalCreators;
    
    private Long totalNotes;
    private Long todayNewNotes;
    private Long weekNewNotes;
    private Long monthNewNotes;
    private Long publishedNotes;
    private Long pendingAuditNotes;
    private Long rejectedNotes;
    
    private Long totalReports;
    private Long pendingReports;
    private Long processingReports;
    private Long processedReports;
}
