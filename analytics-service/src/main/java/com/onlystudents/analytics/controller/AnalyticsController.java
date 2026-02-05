package com.onlystudents.analytics.controller;

import com.onlystudents.analytics.service.CreatorSummaryService;
import com.onlystudents.analytics.service.DailyStatsService;
import com.onlystudents.analytics.service.HourlyStatsService;
import com.onlystudents.analytics.service.NoteStatsService;
import com.onlystudents.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "数据分析", description = "创作者数据中心接口")
public class AnalyticsController {

    private final DailyStatsService dailyStatsService;
    private final CreatorSummaryService creatorSummaryService;
    private final NoteStatsService noteStatsService;
    private final HourlyStatsService hourlyStatsService;

    @GetMapping("/daily/{creatorId}")
    @Operation(summary = "获取每日统计", description = "获取指定创作者在某日期的统计数据")
    public Result<com.onlystudents.analytics.entity.DailyStats> getDailyStats(
            @PathVariable Long creatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dailyStatsService.getDailyStats(creatorId, date));
    }

    @GetMapping("/daily/range/{creatorId}")
    @Operation(summary = "获取日期范围统计", description = "获取指定创作者在日期范围内的统计数据列表")
    public Result<List<com.onlystudents.analytics.entity.DailyStats>> getDailyStatsRange(
            @PathVariable Long creatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyStatsService.getDailyStatsRange(creatorId, startDate, endDate));
    }

    @GetMapping("/daily/summary/{creatorId}")
    @Operation(summary = "获取汇总统计数据", description = "获取指定创作者在日期范围内的汇总统计数据")
    public Result<Map<String, Object>> getSummaryStats(
            @PathVariable Long creatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyStatsService.getSummaryStats(creatorId, startDate, endDate));
    }

    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "获取创作者数据", description = "获取指定创作者的汇总数据")
    public Result<com.onlystudents.analytics.entity.CreatorSummary> getCreatorSummary(@PathVariable Long creatorId) {
        return Result.success(creatorSummaryService.getCreatorSummary(creatorId));
    }

    @GetMapping("/creators/top-revenue")
    @Operation(summary = "获取收入排行榜", description = "按收入获取创作者排行榜")
    public Result<List<com.onlystudents.analytics.entity.CreatorSummary>> getTopCreatorsByRevenue(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(creatorSummaryService.getTopCreatorsByRevenue(limit));
    }

    @GetMapping("/creators/top-followers")
    @Operation(summary = "获取粉丝排行榜", description = "按粉丝数获取创作者排行榜")
    public Result<List<com.onlystudents.analytics.entity.CreatorSummary>> getTopCreatorsByFollowers(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(creatorSummaryService.getTopCreatorsByFollowers(limit));
    }

    @GetMapping("/creators/weekly-top")
    @Operation(summary = "获取周榜", description = "获取周榜创作者列表")
    public Result<List<com.onlystudents.analytics.entity.CreatorSummary>> getWeeklyTopCreators(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(creatorSummaryService.getWeeklyTopCreators(limit));
    }

    @PutMapping("/creator/{creatorId}/heat-score")
    @Operation(summary = "更新创作者热度分", description = "更新指定创作者的热度分")
    public Result<Void> updateCreatorHeatScore(
            @PathVariable Long creatorId,
            @RequestParam BigDecimal heatScore) {
        creatorSummaryService.updateHeatScore(creatorId, heatScore);
        return Result.success();
    }

    @GetMapping("/note/{noteId}")
    @Operation(summary = "获取笔记统计", description = "获取指定笔记的统计数据")
    public Result<com.onlystudents.analytics.entity.NoteStats> getNoteStats(@PathVariable Long noteId) {
        return Result.success(noteStatsService.getNoteStats(noteId));
    }

    @GetMapping("/notes/creator/{creatorId}")
    @Operation(summary = "获取创作者笔记列表", description = "按热度排序获取创作者的笔记列表")
    public Result<List<com.onlystudents.analytics.entity.NoteStats>> getNotesByCreator(@PathVariable Long creatorId) {
        return Result.success(noteStatsService.getNotesByCreatorOrderByHeat(creatorId));
    }

    @GetMapping("/notes/top")
    @Operation(summary = "获取热门笔记", description = "获取热度最高的笔记列表")
    public Result<List<com.onlystudents.analytics.entity.NoteStats>> getTopNotes(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(noteStatsService.getTopNotes(limit));
    }

    @PutMapping("/note/{noteId}/heat-score")
    @Operation(summary = "更新笔记热度分", description = "更新指定笔记的热度分和排名")
    public Result<Void> updateNoteHeatScore(
            @PathVariable Long noteId,
            @RequestParam BigDecimal heatScore,
            @RequestParam Integer ranking) {
        noteStatsService.updateHeatScore(noteId, heatScore, ranking);
        return Result.success();
    }

    @GetMapping("/hourly/{creatorId}")
    @Operation(summary = "获取小时统计数据", description = "获取指定创作者在时间段内的小时统计数据")
    public Result<List<com.onlystudents.analytics.entity.HourlyStats>> getHourlyStats(
            @PathVariable Long creatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(hourlyStatsService.getHourlyStatsRange(creatorId, startTime, endTime));
    }

    @GetMapping("/hourly/peak/{creatorId}")
    @Operation(summary = "获取高峰时段", description = "获取创作者的粉丝活跃高峰时段")
    public Result<List<Map<String, Object>>> getPeakHours(
            @PathVariable Long creatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime) {
        return Result.success(hourlyStatsService.getPeakHours(creatorId, startTime));
    }
}
