package com.onlystudents.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.payment.client.NoteFeignClient;
import com.onlystudents.payment.dto.CreateOrderRequest;
import com.onlystudents.payment.dto.OrderDTO;
import com.onlystudents.payment.dto.PayCallbackRequest;
import com.onlystudents.payment.dto.WalletDTO;
import com.onlystudents.payment.entity.PaymentOrder;
import com.onlystudents.payment.mapper.PaymentOrderMapper;
import com.onlystudents.payment.service.CompensationTaskService;
import com.onlystudents.payment.service.PaymentService;
import com.onlystudents.payment.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentOrderMapper orderMapper;
    private final NoteFeignClient noteFeignClient;
    private final WalletService walletService;
    private final CompensationTaskService compensationTaskService;
    
    @Value("${platform.fee-rate:0.2}")
    private BigDecimal platformFeeRate;
    
    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, Long userId) {
        // 生成订单号
        String orderNo = "ORD" + IdUtil.simpleUUID().toUpperCase();
        
        // 计算平台费和创作者所得
        BigDecimal platformFee = request.getAmount().multiply(platformFeeRate);
        BigDecimal creatorAmount = request.getAmount().subtract(platformFee);
        
        PaymentOrder order = new PaymentOrder();
        BeanUtils.copyProperties(request, order);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPlatformFee(platformFee);
        order.setCreatorAmount(creatorAmount);
        order.setStatus(0); // 待支付
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        
        orderMapper.insert(order);
        
        return convertToDTO(order);
    }
    
    @Override
    public OrderDTO getOrder(String orderNo) {
        PaymentOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        return convertToDTO(order);
    }
    
    @Override
    public List<OrderDTO> getMyOrders(Long userId) {
        List<PaymentOrder> orders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getUserId, userId)
                        .orderByDesc(PaymentOrder::getCreatedAt)
        );
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void handlePayCallback(PayCallbackRequest request) {
        PaymentOrder order = orderMapper.selectByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        if (request.getStatus() == 1) {
            // 支付成功
            order.setStatus(1);
            order.setPayTime(LocalDateTime.now());
            order.setThirdPartyNo(request.getThirdPartyNo());
            orderMapper.updateById(order);
            
            // 增加创作者收入
            Long creatorId = null;
            if (order.getTargetType() == 1) { // 笔记
                try {
                    Result<Map<String, Object>> noteResult = noteFeignClient.getNoteById(order.getTargetId());
                    if (noteResult.getData() != null) {
                        Object userIdObj = noteResult.getData().get("userId");
                        if (userIdObj != null) {
                            creatorId = Long.valueOf(userIdObj.toString());
                        }
                    }
                } catch (Exception e) {
                    log.error("获取笔记创作者失败: noteId={}, orderNo={}", order.getTargetId(), request.getOrderNo(), e);
                }
            } else if (order.getTargetType() == 2) { // 创作者
                creatorId = order.getTargetId();
            }
            
            if (creatorId != null) {
                try {
                    walletService.addIncome(creatorId, order.getId(), order.getCreatorAmount());
                    log.info("订单 [{}] 支付成功，已分配创作者收入: {} 元给创作者 {}", 
                        request.getOrderNo(), order.getCreatorAmount(), creatorId);
                } catch (Exception e) {
                    log.error("订单 [{}] 支付成功，但收入分配失败: {}", request.getOrderNo(), e.getMessage());
                    // 创建补偿任务
                    compensationTaskService.createIncomeCompensationTask(
                        order.getId(), creatorId, order.getCreatorAmount());
                    log.info("已创建收入分配补偿任务: orderId={}, creatorId={}", 
                        order.getId(), creatorId);
                }
            } else {
                log.warn("订单 [{}] 支付成功，但无法确定创作者，收入未分配", request.getOrderNo());
            }
        } else {
            // 支付失败
            order.setStatus(2);
            orderMapper.updateById(order);
        }
    }
    
    @Override
    public WalletDTO getWallet(Long userId) {
        return walletService.getWallet(userId);
    }
    
    @Override
    @Transactional
    public void createWallet(Long userId) {
        walletService.createWallet(userId);
    }
    
    @Override
    @Transactional
    public void addIncome(Long userId, Long orderId, BigDecimal amount) {
        walletService.addIncome(userId, orderId, amount);
    }
    
    @Override
    public boolean checkNotePurchased(Long userId, Long noteId) {
        return orderMapper.checkNotePurchased(userId, noteId);
    }
    
    private OrderDTO convertToDTO(PaymentOrder order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }
}
