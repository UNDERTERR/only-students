package com.onlystudents.subscription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.subscription.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
    
    @Select("SELECT * FROM subscription WHERE subscriber_id = #{subscriberId} AND creator_id = #{creatorId} AND status = 1 LIMIT 1")
    Subscription selectBySubscriberAndCreator(@Param("subscriberId") Long subscriberId, @Param("creatorId") Long creatorId);
    
    @Select("SELECT * FROM subscription WHERE subscriber_id = #{subscriberId} AND status = 1")
    List<Subscription> selectActiveSubscriptionsBySubscriber(Long subscriberId);
    
    @Select("SELECT * FROM subscription WHERE creator_id = #{creatorId} AND status = 1")
    List<Subscription> selectActiveSubscriptionsByCreator(Long creatorId);
    
    @Select("SELECT COUNT(*) FROM subscription WHERE creator_id = #{creatorId} AND status = 1")
    Integer countSubscribersByCreator(Long creatorId);
}
