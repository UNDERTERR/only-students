package com.onlystudents.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.notification.entity.FollowerNotification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowerNotificationMapper extends BaseMapper<FollowerNotification> {
    
    List<FollowerNotification> selectByUserId(Long userId);
    
    @Select("SELECT * FROM follower_notification WHERE to_user_id = #{userId} ORDER BY created_at DESC")
    IPage<FollowerNotification> selectByUserIdPage(Page<FollowerNotification> page, @Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM follower_notification WHERE to_user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(Long userId);
    
    @Insert("INSERT INTO follower_notification (from_user_id, to_user_id, subscription_id, is_read, created_at) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{subscriptionId}, 0, NOW())")
    void insertFollowerNotification(@Param("fromUserId") Long fromUserId,
                                     @Param("toUserId") Long toUserId,
                                     @Param("subscriptionId") Long subscriptionId);
}
