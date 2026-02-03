package com.onlystudents.subscription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.subscription.entity.CreatorSubscriptionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CreatorSubscriptionConfigMapper extends BaseMapper<CreatorSubscriptionConfig> {
    
    @Select("SELECT * FROM creator_subscription_config WHERE creator_id = #{creatorId} LIMIT 1")
    CreatorSubscriptionConfig selectByCreatorId(Long creatorId);
}
