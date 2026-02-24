package com.onlystudents.subscription.service.impl;

import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.subscription.client.UserFeignClient;
import com.onlystudents.subscription.dto.CreatorConfigDTO;
import com.onlystudents.subscription.dto.SubscribeRequest;
import com.onlystudents.subscription.dto.SubscriptionDTO;
import com.onlystudents.subscription.dto.UpdateCreatorConfigRequest;
import com.onlystudents.subscription.entity.CreatorSubscriptionConfig;
import com.onlystudents.subscription.entity.Subscription;
import com.onlystudents.subscription.mapper.CreatorSubscriptionConfigMapper;
import com.onlystudents.subscription.mapper.SubscriptionMapper;
import com.onlystudents.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private final SubscriptionMapper subscriptionMapper;
    private final CreatorSubscriptionConfigMapper configMapper;
    private final UserFeignClient userFeignClient;
    
    @Override
    @Transactional
    public SubscriptionDTO subscribe(SubscribeRequest request, Long subscriberId) {
        // 检查不能订阅自己
        if (subscriberId.equals(request.getCreatorId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能订阅自己");
        }

        // 检查是否已订阅
        Subscription exist = subscriptionMapper.selectBySubscriberAndCreator(subscriberId, request.getCreatorId());
        if (exist != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已订阅该创作者");
        }

        // 新建订阅
        Subscription subscription = new Subscription();
        subscription.setSubscriberId(subscriberId);
        subscription.setCreatorId(request.getCreatorId());

        subscriptionMapper.insert(subscription);
        
        // 更新创作者的粉丝数
        try {
            log.info("开始更新创作者粉丝数, creatorId={}", request.getCreatorId());
            userFeignClient.incrementFollowerCount(request.getCreatorId());
            log.info("开始清除创作者缓存, creatorId={}", request.getCreatorId());
            userFeignClient.clearUserCache(request.getCreatorId());
            log.info("更新粉丝数完成");
        } catch (Exception e) {
            log.error("更新粉丝数失败", e);
        }

        return convertToDTO(subscription);
    }
    
    @Override
    @Transactional
    public void unsubscribe(Long creatorId, Long subscriberId) {
        int deleted = subscriptionMapper.deleteBySubscriberAndCreator(subscriberId, creatorId);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "未订阅该创作者");
        }
        
        // 更新创作者的粉丝数
        try {
            userFeignClient.decrementFollowerCount(creatorId);
            userFeignClient.clearUserCache(creatorId);
        } catch (Exception e) {
            log.error("更新粉丝数失败", e);
        }
    }
    
    @Override
    public Boolean checkSubscription(Long subscriberId, Long creatorId) {
        if (subscriberId == null || creatorId == null) {
            return false;
        }
        Subscription subscription = subscriptionMapper.selectBySubscriberAndCreator(subscriberId, creatorId);
        return subscription != null;
    }
    
    @Override
    public List<SubscriptionDTO> getMySubscriptions(Long subscriberId) {
        List<Subscription> subscriptions = subscriptionMapper.selectBySubscriber(subscriberId);
        
        // 获取所有创作者ID
        List<Long> creatorIds = subscriptions.stream()
                .map(Subscription::getCreatorId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取创作者信息
        Map<Long, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
        log.info("获取订阅列表，创作者IDs: {}", creatorIds);
        if (!creatorIds.isEmpty()) {
            try {
                String idsStr = creatorIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                log.info("调用user-service，ids: {}", idsStr);
                Result<List<Map<String, Object>>> userResult = userFeignClient.getUsersByIds(idsStr);
                log.info("user-service返回结果: {}", userResult);
                if (userResult != null && userResult.getData() != null) {
                    log.info("获取到用户数据条数: {}", userResult.getData().size());
                    for (Map<String, Object> user : userResult.getData()) {
                        log.info("用户数据: {}", user);
                        Object idObj = user.get("id");
                        if (idObj instanceof Number) {
                            userInfoMap.put(((Number) idObj).longValue(), user);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取创作者信息失败", e);
            }
        }
        
        // 转换并填充创作者信息
        Map<Long, Map<String, Object>> finalUserInfoMap = userInfoMap;
        return subscriptions.stream()
                .map(sub -> {
                    SubscriptionDTO dto = convertToDTO(sub);
                    Map<String, Object> userInfo = finalUserInfoMap.get(sub.getCreatorId());
                    if (userInfo != null) {
                        dto.setCreatorNickname(getStringValue(userInfo, "nickname"));
                        dto.setCreatorAvatar(getStringValue(userInfo, "avatar"));
                        dto.setCreatorBio(getStringValue(userInfo, "bio"));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SubscriptionDTO> getMySubscribers(Long creatorId) {
        List<Subscription> subscriptions = subscriptionMapper.selectByCreator(creatorId);
        
        // 获取所有订阅者ID
        List<Long> subscriberIds = subscriptions.stream()
                .map(Subscription::getSubscriberId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取订阅者信息
        Map<Long, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
        log.info("获取粉丝列表，订阅者IDs: {}", subscriberIds);
        if (!subscriberIds.isEmpty()) {
            try {
                String idsStr = subscriberIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                log.info("调用user-service，ids: {}", idsStr);
                Result<List<Map<String, Object>>> userResult = userFeignClient.getUsersByIds(idsStr);
                log.info("user-service返回结果: {}", userResult);
                if (userResult != null && userResult.getData() != null) {
                    log.info("获取到用户数据条数: {}", userResult.getData().size());
                    for (Map<String, Object> user : userResult.getData()) {
                        log.info("用户数据: {}", user);
                        Object idObj = user.get("id");
                        if (idObj instanceof Number) {
                            userInfoMap.put(((Number) idObj).longValue(), user);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取订阅者信息失败", e);
            }
        }
        
        // 转换并填充订阅者信息
        Map<Long, Map<String, Object>> finalUserInfoMap = userInfoMap;
        return subscriptions.stream()
                .map(sub -> {
                    SubscriptionDTO dto = convertToDTO(sub);
                    Map<String, Object> userInfo = finalUserInfoMap.get(sub.getSubscriberId());
                    if (userInfo != null) {
                        dto.setSubscriberNickname(getStringValue(userInfo, "nickname"));
                        dto.setSubscriberAvatar(getStringValue(userInfo, "avatar"));
                        dto.setSubscriberBio(getStringValue(userInfo, "bio"));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer getSubscriberCount(Long creatorId) {
        return subscriptionMapper.countSubscribersByCreator(creatorId);
    }
    
    @Override
    public CreatorConfigDTO getCreatorConfig(Long creatorId) {
        CreatorSubscriptionConfig config = configMapper.selectByCreatorId(creatorId);
        if (config == null) {
            CreatorConfigDTO dto = new CreatorConfigDTO();
            dto.setCreatorId(creatorId);
            dto.setIsEnabled(0);
            return dto;
        }
        return convertToConfigDTO(config);
    }
    
    @Override
    @Transactional
    public CreatorConfigDTO updateCreatorConfig(Long creatorId, UpdateCreatorConfigRequest request) {
        CreatorSubscriptionConfig config = configMapper.selectByCreatorId(creatorId);
        
        if (config == null) {
            config = new CreatorSubscriptionConfig();
            config.setCreatorId(creatorId);
        }
        
        BeanUtils.copyProperties(request, config);
        
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        
        return convertToConfigDTO(config);
    }
    
    @Override
    public Integer getNewFollowerCount(Long creatorId) {
        return subscriptionMapper.countUnreadByCreator(creatorId);
    }
    
    @Override
    public void markFollowerAsRead(Long subscriptionId) {
        Subscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription != null) {
            subscription.setIsRead(1);
            subscriptionMapper.updateById(subscription);
        }
    }
    
    private SubscriptionDTO convertToDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        BeanUtils.copyProperties(subscription, dto);
        return dto;
    }
    
    private CreatorConfigDTO convertToConfigDTO(CreatorSubscriptionConfig config) {
        CreatorConfigDTO dto = new CreatorConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
