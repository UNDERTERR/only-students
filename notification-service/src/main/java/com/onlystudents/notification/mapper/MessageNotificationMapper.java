package com.onlystudents.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.notification.entity.MessageNotification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageNotificationMapper extends BaseMapper<MessageNotification> {
    
    List<MessageNotification> selectByUserId(Long userId);
    
    @Select("SELECT * FROM message_notification WHERE to_user_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    IPage<MessageNotification> selectByUserIdPage(Page<MessageNotification> page, @Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM message_notification WHERE to_user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    Long countUnreadByUserId(Long userId);
    
    @Insert("INSERT INTO message_notification (from_user_id, to_user_id, conversation_id, message_id, is_read, is_deleted, created_at) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{conversationId}, #{messageId}, 0, 0, NOW())")
    void insertMessageNotification(@Param("fromUserId") Long fromUserId,
                                   @Param("toUserId") Long toUserId,
                                   @Param("conversationId") Long conversationId,
                                   @Param("messageId") Long messageId);
    
    int deleteByConversationId(Long conversationId, Long userId);
}
