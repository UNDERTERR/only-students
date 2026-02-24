package com.onlystudents.user.event;

import com.onlystudents.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 用户事件发布器
 * 当用户信息变更时发布到MQ通知其他服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * 发布用户信息更新事件
     */
    public void publishUserInfoUpdated(User user) {
        try {
            // 这里需要等Search-Service创建后才能发布
            //TODO
            log.info("用户信息已更新，等待MQ实现后发布事件: userId={}, nickname={}",
                    user.getId(), user.getNickname());
                    
        } catch (Exception e) {
            log.error("发布用户信息更新事件失败: userId={}", user.getId(), e);
        }
    }
}