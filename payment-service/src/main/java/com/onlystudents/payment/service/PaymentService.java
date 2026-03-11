package com.onlystudents.payment.service;

import com.onlystudents.payment.dto.CreateOrderRequest;
import com.onlystudents.payment.dto.OrderDTO;
import com.onlystudents.payment.dto.PayCallbackRequest;
import com.onlystudents.payment.dto.WalletDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    
    OrderDTO createOrder(CreateOrderRequest request, Long userId);
    
    OrderDTO getOrder(String orderNo);
    
    List<OrderDTO> getMyOrders(Long userId);
    
    void handlePayCallback(PayCallbackRequest request);
    
    WalletDTO getWallet(Long userId);
    
    void createWallet(Long userId);
    
    void addIncome(Long userId, Long orderId, BigDecimal amount);
    
    boolean checkNotePurchased(Long userId, Long noteId);
    
    Long getCreatorRevenue(Long creatorId);
}
