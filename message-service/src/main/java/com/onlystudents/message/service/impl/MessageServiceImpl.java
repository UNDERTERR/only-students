package com.onlystudents.message.service.impl;

import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.message.dto.UserInfoDTO;
import com.onlystudents.message.dto.WebSocketMessage;
import com.onlystudents.message.entity.Conversation;
import com.onlystudents.message.entity.Message;
import com.onlystudents.message.client.UserServiceClient;
import com.onlystudents.message.handler.WebSocketSessionManager;
import com.onlystudents.message.mapper.ConversationMapper;
import com.onlystudents.message.mapper.MessageMapper;
import com.onlystudents.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final WebSocketSessionManager sessionManager;
    private final UserServiceClient userServiceClient;
    
    @Override
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能给自己发送消息");
        }
        
        // 获取或创建会话（一条记录存储两个用户的对话）
        Conversation conversation = getOrCreateConversation(senderId, receiverId);
        
        // 创建消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setContentType(1);
        message.setStatus(0);
        
        messageMapper.insert(message);
        
        // 更新会话最后消息信息
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setLastMessagePreview(content.length() > 200 ? content.substring(0, 200) : content);
        
        // 更新接收者的未读数
        if (receiverId.equals(conversation.getUserId1())) {
            conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
        } else {
            conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
        }
        
        conversationMapper.updateById(conversation);
        
        log.info("用户{}向用户{}发送消息", senderId, receiverId);
        
        // 推送实时消息给接收者（如果在线）
        if (sessionManager.isUserOnline(receiverId)) {
            LocalDateTime now = LocalDateTime.now();
            sessionManager.sendMessageToUser(receiverId, WebSocketMessage.builder()
                    .type("MESSAGE")
                    .data(Map.of(
                            "id", message.getId(),
                            "senderId", senderId,
                            "receiverId", receiverId,
                            "content", content,
                            "createdAt", now.toString()
                    ))
                    .build());
            log.info("消息已通过WebSocket推送给用户{}", receiverId);
        }
        
        return message;
    }
    
    private Conversation getOrCreateConversation(Long userId1, Long userId2) {
        // 确保userId1是较小的ID
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        
        Conversation conversation = conversationMapper.selectByUserIds(smallerId, largerId);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUserId1(smallerId);
            conversation.setUserId2(largerId);
            conversation.setUser1UnreadCount(0);
            conversation.setUser2UnreadCount(0);
            conversation.setUser1Hidden(0);
            conversation.setUser2Hidden(0);
            conversationMapper.insert(conversation);
            log.info("创建新会话: userId1={}, userId2={}", smallerId, largerId);
        }
        return conversation;
    }
    
    @Override
    public List<Conversation> getConversationList(Long userId) {
        List<Conversation> conversations = conversationMapper.selectByUserId(userId);
        
        // 填充用户信息和未读数
        for (Conversation conv : conversations) {
            // 确定当前用户是userId1还是userId2
            boolean isUser1 = userId.equals(conv.getUserId1());
            
            // 设置目标用户ID（对方）
            Long targetId = isUser1 ? conv.getUserId2() : conv.getUserId1();
            conv.setTargetUserId(targetId);
            
            // 设置未读数和最后消息
            if (isUser1) {
                conv.setUnreadCount(conv.getUser1UnreadCount());
            } else {
                conv.setUnreadCount(conv.getUser2UnreadCount());
            }
            conv.setLastMessage(conv.getLastMessagePreview());
            
            // 查询用户信息
            try {
                Result<UserInfoDTO> userResult = userServiceClient.getUserById(targetId);
                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                    UserInfoDTO userData = userResult.getData();
                    conv.setTargetUserName(userData.getUsername());
                    conv.setTargetNickname(userData.getNickname());
                    conv.setTargetUserAvatar(userData.getAvatar());
                }
            } catch (Exception e) {
                log.warn("获取用户{}信息失败: {}", targetId, e.getMessage());
                conv.setTargetUserName("用户" + targetId);
            }
        }
        
        return conversations;
    }
    
    @Override
    public List<Message> getMessageHistory(Long conversationId, Long userId, Integer page, Integer size) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        // 检查用户是否参与该会话
        if (!userId.equals(conversation.getUserId1()) && !userId.equals(conversation.getUserId2())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        
        int offset = (page - 1) * size;
        return messageMapper.selectByConversationIdWithPagination(conversationId, offset, size);
    }
    
    @Override
    @Transactional
    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null || !message.getReceiverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该消息");
        }
        
        // 消息状态改为已读
        message.setStatus(1);
        messageMapper.updateById(message);
        
        // 更新会话未读数
        Conversation conversation = conversationMapper.selectById(message.getConversationId());
        if (conversation != null) {
            if (userId.equals(conversation.getUserId1()) && conversation.getUser1UnreadCount() > 0) {
                conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() - 1);
                conversationMapper.updateById(conversation);
            } else if (userId.equals(conversation.getUserId2()) && conversation.getUser2UnreadCount() > 0) {
                conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() - 1);
                conversationMapper.updateById(conversation);
            }
        }
    }
    
    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        
        // 检查用户是否参与该会话
        boolean isUser1 = userId.equals(conversation.getUserId1());
        boolean isUser2 = userId.equals(conversation.getUserId2());
        
        if (!isUser1 && !isUser2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该会话");
        }
        
        // 将该会话的所有未读消息标记为已读
        messageMapper.markAllAsReadByConversationId(conversationId, userId);
        
        // 清零未读数
        if (isUser1) {
            conversation.setUser1UnreadCount(0);
        } else {
            conversation.setUser2UnreadCount(0);
        }
        conversationMapper.updateById(conversation);
        
        log.info("用户{}将会话{}的所有消息标记为已读", userId, conversationId);
    }
    
    @Override
    public Long getTotalUnreadCount(Long userId) {
        List<Conversation> conversations = conversationMapper.selectByUserId(userId);
        long total = 0;
        for (Conversation conv : conversations) {
            if (userId.equals(conv.getUserId1())) {
                total += conv.getUser1UnreadCount();
            } else {
                total += conv.getUser2UnreadCount();
            }
        }
        return total;
    }
    
    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        
        // 检查用户是否参与该会话
        boolean isUser1 = userId.equals(conversation.getUserId1());
        boolean isUser2 = userId.equals(conversation.getUserId2());
        
        if (!isUser1 && !isUser2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该会话");
        }
        
        // 标记该用户的隐藏状态，而不是真正删除
        if (isUser1) {
            conversation.setUser1Hidden(1);
        } else {
            conversation.setUser2Hidden(1);
        }
        
        conversationMapper.updateById(conversation);
        log.info("用户{}隐藏会话{}", userId, conversationId);
    }
}
