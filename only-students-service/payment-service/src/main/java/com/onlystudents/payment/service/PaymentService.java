package com.onlystudents.payment.service;

import com.onlystudents.payment.dto.*;

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
}
