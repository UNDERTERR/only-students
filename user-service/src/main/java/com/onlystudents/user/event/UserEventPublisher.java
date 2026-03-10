package com.onlystudents.user.event;

import com.onlystudents.common.event.user.UserInfoUpdatedEvent;
import com.onlystudents.user.config.RabbitConfig;
import com.onlystudents.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishUserInfoUpdated(User user) {
        try {
            UserInfoUpdatedEvent event = new UserInfoUpdatedEvent(
                user.getId(),
                user.getNickname(),
                user.getAvatar(),
                user.getBio()
            );
            rabbitTemplate.convertAndSend(
                RabbitConfig.USER_INFO_EXCHANGE,
                RabbitConfig.USER_INFO_UPDATE_ROUTING_KEY,
                event
            );
            log.info("发布用户信息更新事件: userId={}, nickname={}", user.getId(), user.getNickname());
        } catch (Exception e) {
            log.error("发布用户信息更新事件失败: userId={}", user.getId(), e);
        }
    }
}