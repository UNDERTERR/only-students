package com.onlystudents.rating.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.CreateFolderRequest;
import com.onlystudents.rating.dto.FavoriteFolderDTO;
import com.onlystudents.rating.dto.NoteFavoriteDTO;
import com.onlystudents.rating.service.NoteFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "收藏夹ID") @RequestParam(required = false) Long folderId) {
        if (folderId != null) {
            return favoriteService.getUserFavorites(userId, folderId);
        }
        return favoriteService.getUserFavorites(userId);
    }
    
    @GetMapping("/folders")
    @Operation(summary = "我的收藏夹", description = "获取当前用户的收藏夹列表")
    public Result<List<FavoriteFolderDTO>> getMyFolders(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.getUserFolders(userId);
    }
    
    @PostMapping("/folders")
    @Operation(summary = "创建收藏夹", description = "创建一个新的收藏夹")
    public Result<FavoriteFolderDTO> createFolder(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateFolderRequest request) {
        return favoriteService.createFolder(userId, request);
    }
    
    @PutMapping("/note/{noteId}/folder")
    @Operation(summary = "移动收藏到文件夹", description = "通过笔记ID将收藏移动到指定收藏夹")
    public Result<Void> moveFavoriteByNoteId(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "收藏夹ID") @RequestParam Long folderId) {
        return favoriteService.moveFavoriteByNoteId(noteId, userId, folderId);
    }


    
    @DeleteMapping("/folders/{folderId}")
    @Operation(summary = "删除收藏夹", description = "删除指定收藏夹，笔记不会被取消收藏")
    public Result<Void> deleteFolder(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.deleteFolder(folderId, userId);
    }
    
    @GetMapping("/my-notifiers")
    @Operation(summary = "谁收藏了我的笔记", description = "获取收藏了我发布的笔记的用户列表")
    public Result<List<NoteFavoriteDTO>> getMyNoteFavorites(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return favoriteService.getMyNoteFavorites(userId, page, size);
    }
    
    @GetMapping("/my-notifiers/count")
    @Operation(summary = "我的笔记被收藏未读数", description = "获取我的笔记被收藏的未读数量")
    public Result<Long> getMyNoteFavoriteUnreadCount(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.getMyNoteFavoriteUnreadCount(userId);
    }
    
    @PostMapping("/my-notifiers/{favoriteId}/read")
    @Operation(summary = "标记收藏为已读", description = "将我的笔记被收藏的记录标记为已读")
    public Result<Void> markFavoriteAsRead(
            @Parameter(description = "收藏记录ID") @PathVariable Long favoriteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.markFavoriteAsRead(favoriteId, userId);
    }
}
