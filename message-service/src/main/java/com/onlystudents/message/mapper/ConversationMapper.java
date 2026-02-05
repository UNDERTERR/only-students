package com.onlystudents.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.message.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} AND deleted = 0 ORDER BY last_message_time DESC")
    List<Conversation> selectByUserId(Long userId);
    
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} AND target_user_id = #{targetUserId} AND deleted = 0")
    Conversation selectByUserIdAndTargetUserId(Long userId, Long targetUserId);
}
