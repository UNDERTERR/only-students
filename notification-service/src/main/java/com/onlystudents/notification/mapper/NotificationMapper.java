package com.onlystudents.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Notification> selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND is_read = 0 ORDER BY created_at DESC")
    List<Notification> selectUnreadByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE id = #{id}")
    void markAsRead(@Param("id") Long id);
    
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    void markAllAsRead(@Param("userId") Long userId);
}
