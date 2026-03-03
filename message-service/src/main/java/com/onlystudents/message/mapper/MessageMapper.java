package com.onlystudents.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY created_at DESC")
    List<Message> selectByConversationId(Long conversationId);
    
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Message> selectByConversationIdWithPagination(Long conversationId, int offset, int limit);
    
    @Select("SELECT COUNT(*) FROM message WHERE conversation_id = #{conversationId} AND deleted = 0")
    Long countByConversationId(Long conversationId);
    
    @Update("UPDATE message SET status = 1 WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND status = 0 AND deleted = 0")
    int markAllAsReadByConversationId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
