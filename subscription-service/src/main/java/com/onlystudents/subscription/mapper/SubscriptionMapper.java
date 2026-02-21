package com.onlystudents.subscription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.subscription.entity.Subscription;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
    
    @Select("SELECT * FROM subscription WHERE subscriber_id = #{subscriberId} AND creator_id = #{creatorId} LIMIT 1")
    Subscription selectBySubscriberAndCreator(@Param("subscriberId") Long subscriberId, @Param("creatorId") Long creatorId);

    @Select("SELECT * FROM subscription WHERE subscriber_id = #{subscriberId}")
    List<Subscription> selectBySubscriber(Long subscriberId);

    @Select("SELECT * FROM subscription WHERE creator_id = #{creatorId}")
    List<Subscription> selectByCreator(Long creatorId);

    @Select("SELECT COUNT(*) FROM subscription WHERE creator_id = #{creatorId}")
    Integer countSubscribersByCreator(Long creatorId);

    @Delete("DELETE FROM subscription WHERE subscriber_id = #{subscriberId} AND creator_id = #{creatorId}")
    int deleteBySubscriberAndCreator(@Param("subscriberId") Long subscriberId, @Param("creatorId") Long creatorId);
    
    @Select("SELECT COUNT(*) FROM subscription WHERE creator_id = #{creatorId} AND (is_read = 0 OR is_read IS NULL)")
    Integer countUnreadByCreator(Long creatorId);
}
