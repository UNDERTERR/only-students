package com.onlystudents.admin.service;

import com.onlystudents.admin.dto.response.AdminStatsResponse;
import com.onlystudents.admin.dto.response.UserStatsDTO;
import com.onlystudents.admin.dto.response.NoteStatsDTO;
import com.onlystudents.admin.dto.response.ReportStatsDTO;
import com.onlystudents.admin.client.UserFeignClient;
import com.onlystudents.admin.client.NoteFeignClient;
import com.onlystudents.admin.client.ReportFeignClient;
import com.onlystudents.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserFeignClient userFeignClient;
    private final NoteFeignClient noteFeignClient;
    private final ReportFeignClient reportFeignClient;

    public AdminStatsResponse getAdminStats() {
        AdminStatsResponse stats = new AdminStatsResponse();
        
        try {
            Result userResult = userFeignClient.getUserStats();
            if (userResult != null && userResult.getData() != null) {
                UserStatsDTO userStats = (UserStatsDTO) userResult.getData();
                stats.setTotalUsers(userStats.getTotalUsers());
                stats.setTodayNewUsers(userStats.getTodayNewUsers());
                stats.setWeekNewUsers(userStats.getWeekNewUsers());
                stats.setMonthNewUsers(userStats.getMonthNewUsers());
                stats.setTotalCreators(userStats.getTotalCreators());
            }
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
        }
        
        try {
            Result noteResult = noteFeignClient.getNoteStats();
            if (noteResult != null && noteResult.getData() != null) {
                NoteStatsDTO noteStats = (NoteStatsDTO) noteResult.getData();
                stats.setTotalNotes(noteStats.getTotalNotes());
                stats.setTodayNewNotes(noteStats.getTodayNewNotes());
                stats.setWeekNewNotes(noteStats.getWeekNewNotes());
                stats.setMonthNewNotes(noteStats.getMonthNewNotes());
                stats.setPublishedNotes(noteStats.getPublishedNotes());
                stats.setPendingAuditNotes(noteStats.getPendingAuditNotes());
                stats.setRejectedNotes(noteStats.getRejectedNotes());
            }
        } catch (Exception e) {
            log.error("获取笔记统计失败", e);
        }
        
        try {
            Result reportResult = reportFeignClient.getReportStats();
            if (reportResult != null && reportResult.getData() != null) {
                ReportStatsDTO reportStats = (ReportStatsDTO) reportResult.getData();
                stats.setTotalReports(reportStats.getTotalReports());
                stats.setPendingReports(reportStats.getPendingReports());
                stats.setProcessingReports(reportStats.getProcessingReports());
                stats.setProcessedReports(reportStats.getProcessedReports());
            }
        } catch (Exception e) {
            log.error("获取举报统计失败", e);
        }
        
        return stats;
    }
}
