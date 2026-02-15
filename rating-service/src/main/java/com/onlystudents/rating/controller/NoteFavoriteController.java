package com.onlystudents.rating.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.NoteFavoriteDTO;
import com.onlystudents.rating.service.NoteFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记收藏控制器
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Tag(name = "笔记收藏管理", description = "笔记收藏/取消收藏等接口")
public class NoteFavoriteController {
    
    private final NoteFavoriteService favoriteService;
    
    @PostMapping("/{noteId}")
    @Operation(summary = "收藏笔记", description = "收藏指定笔记")
    public Result<Void> favoriteNote(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "收藏夹ID") @RequestParam(required = false) Long folderId) {
        return favoriteService.favoriteNote(noteId, userId, folderId);
    }
    
    @DeleteMapping("/{noteId}")
    @Operation(summary = "取消收藏", description = "取消收藏指定笔记")
    public Result<Void> unfavoriteNote(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.unfavoriteNote(noteId, userId);
    }
    
    @GetMapping("/check/{noteId}")
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏该笔记")
    public Result<Boolean> isFavorited(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.isFavorited(noteId, userId);
    }
    
    @GetMapping("/count/{noteId}")
    @Operation(summary = "获取收藏数", description = "获取笔记的收藏数量")
    public Result<Long> getCount(
            @Parameter(description = "笔记ID") @PathVariable Long noteId) {
        return favoriteService.getFavoriteCount(noteId);
    }
    
    @GetMapping("/my")
    @Operation(summary = "我的收藏", description = "获取当前用户的收藏列表")
    public Result<List<NoteFavoriteDTO>> getMyFavorites(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.getUserFavorites(userId);
    }
}
