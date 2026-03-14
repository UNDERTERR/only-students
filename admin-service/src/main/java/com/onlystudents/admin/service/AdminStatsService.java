package com.onlystudents.admin.service;

import com.onlystudents.admin.dto.response.AdminStatsResponse;
import com.onlystudents.admin.client.UserFeignClient;
import com.onlystudents.admin.client.NoteFeignClient;
import com.onlystudents.admin.client.ReportFeignClient;
import com.onlystudents.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.onlystudents.common.utils.TypeConvertUtils.toLong;
import java.util.Map;

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
                Map<String, Object> userStats = (Map<String, Object>) userResult.getData();
                stats.setTotalUsers(toLong(userStats.get("totalUsers")));
                stats.setTodayNewUsers(toLong(userStats.get("todayNewUsers")));
                stats.setWeekNewUsers(toLong(userStats.get("weekNewUsers")));
                stats.setMonthNewUsers(toLong(userStats.get("monthNewUsers")));
                stats.setTotalCreators(toLong(userStats.get("totalCreators")));
            }
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
        }
        
        try {
            Result noteResult = noteFeignClient.getNoteStats();
            if (noteResult != null && noteResult.getData() != null) {
                Map<String, Object> noteStats = (Map<String, Object>) noteResult.getData();
                stats.setTotalNotes(toLong(noteStats.get("totalNotes")));
                stats.setTodayNewNotes(toLong(noteStats.get("todayNewNotes")));
                stats.setWeekNewNotes(toLong(noteStats.get("weekNewNotes")));
                stats.setMonthNewNotes(toLong(noteStats.get("monthNewNotes")));
                stats.setPublishedNotes(toLong(noteStats.get("publishedNotes")));
                stats.setPendingAuditNotes(toLong(noteStats.get("pendingAuditNotes")));
                stats.setRejectedNotes(toLong(noteStats.get("rejectedNotes")));
            }
        } catch (Exception e) {
            log.error("获取笔记统计失败", e);
        }
        
        try {
            Result reportResult = reportFeignClient.getReportStats();
            if (reportResult != null && reportResult.getData() != null) {
                Map<String, Object> reportStats = (Map<String, Object>) reportResult.getData();
                stats.setTotalReports(toLong(reportStats.get("totalReports")));
                stats.setPendingReports(toLong(reportStats.get("pendingReports")));
                stats.setProcessingReports(toLong(reportStats.get("processingReports")));
                stats.setProcessedReports(toLong(reportStats.get("processedReports")));
            }
        } catch (Exception e) {
            log.error("获取举报统计失败", e);
        }
        
        return stats;
    }

}
