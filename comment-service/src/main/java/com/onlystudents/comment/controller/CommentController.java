package com.onlystudents.comment.controller;

import com.onlystudents.comment.dto.CommentDTO;
import com.onlystudents.comment.dto.CreateCommentRequest;
import com.onlystudents.comment.service.CommentService;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论/点赞/回复等接口")
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping
    @Operation(summary = "发表评论", description = "发表评论或回复")
    public Result<CommentDTO> createComment(@RequestBody CreateCommentRequest request,
                                             @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(commentService.createComment(request, userId));
    }
    
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论", description = "删除自己的评论")
    public Result<Void> deleteComment(@PathVariable Long commentId,
                                       @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        commentService.deleteComment(commentId, userId);
        return Result.success();
    }
    
    @GetMapping("/note/{noteId}")
    @Operation(summary = "笔记评论", description = "获取笔记的所有评论")
    public Result<List<CommentDTO>> getNoteComments(@PathVariable Long noteId,
                                                     @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        return Result.success(commentService.getNoteComments(noteId, userId));
    }
    
    @PostMapping("/{commentId}/like")
    @Operation(summary = "点赞评论", description = "给评论点赞")
    public Result<Void> likeComment(@PathVariable Long commentId,
                                     @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        commentService.likeComment(commentId, userId);
        return Result.success();
    }
    
    @DeleteMapping("/{commentId}/like")
    @Operation(summary = "取消点赞", description = "取消评论点赞")
    public Result<Void> unlikeComment(@PathVariable Long commentId,
                                       @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        commentService.unlikeComment(commentId, userId);
        return Result.success();
    }
    
    @GetMapping("/count/{noteId}")
    @Operation(summary = "评论数量", description = "获取笔记的评论数量")
    public Result<Integer> getCommentCount(@PathVariable Long noteId) {
        return Result.success(commentService.getCommentCount(noteId));
    }
    
    @GetMapping("/{commentId}")
    @Operation(summary = "评论详情", description = "获取单条评论的详情，包含根评论和所有回复")
    public Result<CommentDTO> getCommentDetail(@PathVariable Long commentId,
                                               @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        return Result.success(commentService.getCommentDetail(commentId, userId));
    }
    
    @GetMapping("/received")
    @Operation(summary = "收到的评论", description = "获取当前用户收到的评论（别人评论我的笔记）")
    public Result<List<CommentDTO>> getReceivedComments(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                         @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(commentService.getReceivedComments(userId, page, size));
    }
    
    @GetMapping("/sent")
    @Operation(summary = "发出的评论", description = "获取当前用户发出的评论")
    public Result<List<CommentDTO>> getSentComments(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                      @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                      @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(commentService.getSentComments(userId, page, size));
    }
    
    @GetMapping("/received/count")
    @Operation(summary = "收到的评论未读数", description = "获取当前用户收到的评论未读数")
    public Result<Integer> getReceivedCommentCount(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(commentService.getReceivedCommentUnreadCount(userId));
    }
    
    @PostMapping("/received/{commentId}/read")
    @Operation(summary = "标记评论已读", description = "将收到的评论标记为已读")
    public Result<Void> markCommentAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                           @PathVariable Long commentId) {
        commentService.markCommentAsRead(commentId);
        return Result.success();
    }
}
