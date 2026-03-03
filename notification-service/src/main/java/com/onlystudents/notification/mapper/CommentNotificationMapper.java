package com.onlystudents.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.notification.entity.CommentNotification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentNotificationMapper extends BaseMapper<CommentNotification> {
    
    List<CommentNotification> selectByUserId(Long userId);
    
    @Select("SELECT * FROM comment_notification WHERE to_user_id = #{userId} ORDER BY created_at DESC")
    IPage<CommentNotification> selectByUserIdPage(Page<CommentNotification> page, @Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM comment_notification WHERE to_user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(Long userId);
    
    @Insert("INSERT INTO comment_notification (from_user_id, to_user_id, note_id, comment_id, is_read, created_at) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{noteId}, #{commentId}, 0, NOW())")
    void insertCommentNotification(@Param("fromUserId") Long fromUserId,
                                    @Param("toUserId") Long toUserId,
                                    @Param("noteId") Long noteId,
                                    @Param("commentId") Long commentId);
}
