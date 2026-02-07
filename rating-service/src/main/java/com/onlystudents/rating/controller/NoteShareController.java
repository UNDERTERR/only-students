package com.onlystudents.rating.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.NoteShareDTO;
import com.onlystudents.rating.service.NoteShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记分享控制器
 */
@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
@Tag(name = "笔记分享管理", description = "笔记分享/查询分享等接口")
public class NoteShareController {
    
    private final NoteShareService shareService;
    
    @PostMapping("/{noteId}")
    @Operation(summary = "创建分享", description = "创建笔记分享链接")
    public Result<NoteShareDTO> createShare(
            @Parameter(description = "笔记ID") @PathVariable Long noteId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "分享类型(1-微信 2-QQ 3-链接)") @RequestParam(name = "shareType") Integer shareType) {
        return shareService.createShare(noteId, userId, shareType);
    }
    
    @GetMapping("/code/{shareCode}")
    @Operation(summary = "获取分享信息", description = "根据分享码获取分享详情")
    public Result<NoteShareDTO> getShareByCode(
            @Parameter(description = "分享码") @PathVariable String shareCode) {
        return shareService.getShareByCode(shareCode);
    }
    
    @PostMapping("/click/{shareCode}")
    @Operation(summary = "点击分享", description = "记录分享链接点击")
    public Result<Void> clickShare(
            @Parameter(description = "分享码") @PathVariable String shareCode) {
        return shareService.incrementClickCount(shareCode);
    }
    
    @GetMapping("/count/{noteId}")
    @Operation(summary = "获取分享数", description = "获取笔记的分享数量")
    public Result<Long> getShareCount(
            @Parameter(description = "笔记ID") @PathVariable Long noteId) {
        return shareService.getShareCount(noteId);
    }
    
    @GetMapping("/my")
    @Operation(summary = "我的分享", description = "获取当前用户的分享列表")
    public Result<List<NoteShareDTO>> getMyShares(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return shareService.getUserShares(userId);
    }
}
