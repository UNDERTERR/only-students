package com.onlystudents.message.service.impl;

import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.message.entity.Conversation;
import com.onlystudents.message.entity.Message;
import com.onlystudents.message.mapper.ConversationMapper;
import com.onlystudents.message.mapper.MessageMapper;
import com.onlystudents.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    
    @Override
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能给自己发送消息");
        }
        
        // 获取或创建会话
        Conversation conversation = getOrCreateConversation(senderId, receiverId);
        
        // 创建消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(1);
        message.setStatus(0);
        
        messageMapper.insert(message);
        
        // 更新会话最后消息
        conversation.setLastMessage(content);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);
        
        // 更新对方会话的未读数
        Conversation targetConversation = conversationMapper.selectByUserIdAndTargetUserId(receiverId, senderId);
        if (targetConversation != null) {
            targetConversation.setLastMessage(content);
            targetConversation.setLastMessageTime(LocalDateTime.now());
            targetConversation.setUnreadCount(targetConversation.getUnreadCount() + 1);
            targetConversation.setUpdatedAt(LocalDateTime.now());
            conversationMapper.updateById(targetConversation);
        }
        
        log.info("用户{}向用户{}发送消息", senderId, receiverId);
        return message;
    }
    
    private Conversation getOrCreateConversation(Long userId, Long targetUserId) {
        Conversation conversation = conversationMapper.selectByUserIdAndTargetUserId(userId, targetUserId);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setTargetUserId(targetUserId);
            conversation.setUnreadCount(0);
            conversation.setStatus(1);
            conversationMapper.insert(conversation);
            
            // 为对方创建会话
            Conversation targetConversation = new Conversation();
            targetConversation.setUserId(targetUserId);
            targetConversation.setTargetUserId(userId);
            targetConversation.setUnreadCount(0);
            targetConversation.setStatus(1);
            conversationMapper.insert(targetConversation);
        }
        return conversation;
    }
    
    @Override
    public List<Conversation> getConversationList(Long userId) {
        return conversationMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Message> getMessageHistory(Long conversationId, Long userId, Integer page, Integer size) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
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
        
        message.setStatus(1);
        messageMapper.updateById(message);
        
        // 更新会话未读数
        Conversation conversation = conversationMapper.selectById(message.getConversationId());
        if (conversation != null && conversation.getUnreadCount() > 0) {
            conversation.setUnreadCount(conversation.getUnreadCount() - 1);
            conversationMapper.updateById(conversation);
        }
    }
    
    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该会话");
        }
        
        conversationMapper.deleteById(conversationId);
        log.info("用户{}删除会话{}", userId, conversationId);
    }
}
