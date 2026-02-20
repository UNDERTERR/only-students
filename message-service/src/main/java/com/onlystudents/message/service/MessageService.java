package com.onlystudents.message.service;

import com.onlystudents.message.entity.Conversation;
import com.onlystudents.message.entity.Message;

import java.util.List;

public interface MessageService {
    
    Message sendMessage(Long senderId, Long receiverId, String content);
    
    List<Conversation> getConversationList(Long userId);
    
    List<Message> getMessageHistory(Long conversationId, Long userId, Integer page, Integer size);
    
    void markMessageAsRead(Long messageId, Long userId);
    
    void markConversationAsRead(Long conversationId, Long userId);
    
    Long getTotalUnreadCount(Long userId);
    
    void deleteConversation(Long conversationId, Long userId);
}
