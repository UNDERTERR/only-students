package com.onlystudents.message.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.message.entity.Conversation;
import com.onlystudents.message.entity.Message;
import com.onlystudents.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "私信管理", description = "发送消息、获取会话列表、获取消息历史等接口")
public class MessageController {
    
    private final MessageService messageService;
    
    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "向指定用户发送私信")
    public Result<Message> sendMessage(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                       @RequestParam Long receiverId,
                                       @RequestParam String content) {
        return Result.success(messageService.sendMessage(userId, receiverId, content));
    }
    
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话列表")
    public Result<List<Conversation>> getConversationList(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(messageService.getConversationList(userId));
    }
    
    @GetMapping("/history/{conversationId}")
    @Operation(summary = "获取消息历史", description = "获取指定会话的消息历史")
    public Result<List<Message>> getMessageHistory(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                  @PathVariable Long conversationId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(messageService.getMessageHistory(conversationId, userId, page, size));
    }
    
    @PostMapping("/read/{messageId}")
    @Operation(summary = "标记消息已读", description = "将指定消息标记为已读状态")
    public Result<Void> markMessageAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                         @PathVariable Long messageId) {
        messageService.markMessageAsRead(messageId, userId);
        return Result.success();
    }
    
    @DeleteMapping("/conversation/{conversationId}")
    @Operation(summary = "删除会话", description = "删除指定的会话及其所有消息")
    public Result<Void> deleteConversation(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                          @PathVariable Long conversationId) {
        messageService.deleteConversation(conversationId, userId);
        return Result.success();
    }
}
