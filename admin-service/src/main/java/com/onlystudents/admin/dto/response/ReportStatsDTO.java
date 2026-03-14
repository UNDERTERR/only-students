package com.onlystudents.admin.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class ReportStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long totalReports;
    private Long pendingReports;
    private Long processingReports;
    private Long processedReports;
    private Long rejectedReports;
    private Long todayReports;
}
