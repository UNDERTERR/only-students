package com.onlystudents.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<Notification> selectByUserId(Long userId);
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND status = 0 AND deleted = 0 ORDER BY created_at DESC")
    List<Notification> selectUnreadByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND status = 0 AND deleted = 0")
    Long countUnreadByUserId(Long userId);
    
    @Update("UPDATE notification SET status = 1, read_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    void markAsRead(Long id);
    
    @Update("UPDATE notification SET status = 1, read_time = NOW(), updated_at = NOW() WHERE user_id = #{userId} AND status = 0")
    void markAllAsRead(Long userId);
}
