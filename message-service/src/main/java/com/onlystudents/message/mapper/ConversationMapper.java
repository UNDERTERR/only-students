package com.onlystudents.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.message.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    @Select("SELECT * FROM conversation WHERE (user_id_1 = #{userId} AND user_1_hidden = 0) OR (user_id_2 = #{userId} AND user_2_hidden = 0) ORDER BY last_message_time DESC")
    List<Conversation> selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM conversation WHERE ((user_id_1 = #{userId1} AND user_id_2 = #{userId2}) OR (user_id_1 = #{userId2} AND user_id_2 = #{userId1})) AND user_1_hidden = 0 AND user_2_hidden = 0")
    Conversation selectByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
