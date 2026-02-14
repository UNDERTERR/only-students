package com.onlystudents.subscription.service.impl;

import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private final SubscriptionMapper subscriptionMapper;
    private final CreatorSubscriptionConfigMapper configMapper;
    
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

        return convertToDTO(subscription);
    }
    
    @Override
    @Transactional
    public void unsubscribe(Long creatorId, Long subscriberId) {
        int deleted = subscriptionMapper.deleteBySubscriberAndCreator(subscriberId, creatorId);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "未订阅该创作者");
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
        return subscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SubscriptionDTO> getMySubscribers(Long creatorId) {
        List<Subscription> subscriptions = subscriptionMapper.selectByCreator(creatorId);
        return subscriptions.stream()
                .map(this::convertToDTO)
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
}
