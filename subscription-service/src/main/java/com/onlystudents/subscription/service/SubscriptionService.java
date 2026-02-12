package com.onlystudents.subscription.service;

import com.onlystudents.subscription.dto.CreatorConfigDTO;
import com.onlystudents.subscription.dto.SubscribeRequest;
import com.onlystudents.subscription.dto.SubscriptionDTO;
import com.onlystudents.subscription.dto.UpdateCreatorConfigRequest;

import java.util.List;

public interface SubscriptionService {
    
    SubscriptionDTO subscribe(SubscribeRequest request, Long subscriberId);
    
    void unsubscribe(Long creatorId, Long subscriberId);
    
    Boolean checkSubscription(Long subscriberId, Long creatorId);
    
    List<SubscriptionDTO> getMySubscriptions(Long subscriberId);
    
    List<SubscriptionDTO> getMySubscribers(Long creatorId);
    
    Integer getSubscriberCount(Long creatorId);
    
    CreatorConfigDTO getCreatorConfig(Long creatorId);
    
    CreatorConfigDTO updateCreatorConfig(Long creatorId, UpdateCreatorConfigRequest request);
}
