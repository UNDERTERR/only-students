package com.onlystudents.rating.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.NoteRatingDTO;
import com.onlystudents.rating.service.NoteRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记评分控制器
 */
@RestController
@RequestMapping("/rating")
@RequiredArgsConstructor
@Tag(name = "笔记评分管理", description = "笔记评分/查询评分等接口")
public class NoteRatingController {
    
    private final NoteRatingService ratingService;
    
    @PostMapping("/{noteId}")
    @Operation(summary = "评分笔记", description = "给笔记打分(1-5星)")
    public Result<Void> rateNote(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "评分(1-5)") @RequestParam(name = "score") Integer score) {
        return ratingService.rateNote(noteId, userId, score);
    }
    
    @GetMapping("/average/{noteId}")
    @Operation(summary = "获取平均评分", description = "获取笔记的平均评分")
    public Result<Double> getAverageRating(
            @Parameter(description = "笔记ID") @PathVariable Long noteId) {
        return ratingService.getAverageRating(noteId);
    }
    
    @GetMapping("/count/{noteId}")
    @Operation(summary = "获取评分人数", description = "获取笔记的评分人数")
    public Result<Long> getRatingCount(
            @Parameter(description = "笔记ID") @PathVariable Long noteId) {
        return ratingService.getRatingCount(noteId);
    }
    
    @GetMapping("/my/{noteId}")
    @Operation(summary = "获取我的评分", description = "获取当前用户对笔记的评分")
    public Result<NoteRatingDTO> getMyRating(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ratingService.getUserRating(noteId, userId);
    }
    
    @GetMapping("/my")
    @Operation(summary = "我的评分", description = "获取当前用户的所有评分")
    public Result<List<NoteRatingDTO>> getMyRatings(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ratingService.getUserRatings(userId);
    }
}
