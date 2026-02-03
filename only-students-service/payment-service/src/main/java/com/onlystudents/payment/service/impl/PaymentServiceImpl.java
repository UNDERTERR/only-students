package com.onlystudents.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.onlystudents.common.core.exception.BusinessException;
import com.onlystudents.common.core.result.Result;
import com.onlystudents.common.core.result.ResultCode;
import com.onlystudents.payment.client.NoteFeignClient;
import com.onlystudents.payment.dto.*;
import com.onlystudents.payment.entity.PaymentOrder;
import com.onlystudents.payment.entity.Wallet;
import com.onlystudents.payment.entity.WalletTransaction;
import com.onlystudents.payment.mapper.PaymentOrderMapper;
import com.onlystudents.payment.mapper.WalletMapper;
import com.onlystudents.payment.mapper.WalletTransactionMapper;
import com.onlystudents.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentOrderMapper orderMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper transactionMapper;
    private final NoteFeignClient noteFeignClient;
    
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
                addIncome(creatorId, order.getId(), order.getCreatorAmount());
                log.info("订单 [{}] 支付成功，已分配创作者收入: {} 元给创作者 {}", 
                    request.getOrderNo(), order.getCreatorAmount(), creatorId);
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
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            // 自动创建钱包
            createWallet(userId);
            wallet = walletMapper.selectByUserId(userId);
        }
        return convertToWalletDTO(wallet);
    }
    
    @Override
    @Transactional
    public void createWallet(Long userId) {
        Wallet exist = walletMapper.selectByUserId(userId);
        if (exist != null) {
            return;
        }
        
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenAmount(BigDecimal.ZERO);
        wallet.setTotalIncome(BigDecimal.ZERO);
        wallet.setTotalWithdrawal(BigDecimal.ZERO);
        
        walletMapper.insert(wallet);
    }
    
    @Override
    @Transactional
    public void addIncome(Long userId, Long orderId, BigDecimal amount) {
        // 增加余额
        walletMapper.addIncome(userId, amount);
        
        // 记录流水
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setType(1); // 收入
        transaction.setAmount(amount);
        
        Wallet wallet = walletMapper.selectByUserId(userId);
        transaction.setBalance(wallet.getBalance().add(amount));
        transaction.setRelatedOrderNo(orderId.toString());
        transaction.setRemark("订单收入");
        
        transactionMapper.insert(transaction);
    }
    
    private OrderDTO convertToDTO(PaymentOrder order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }
    
    private WalletDTO convertToWalletDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        BeanUtils.copyProperties(wallet, dto);
        return dto;
    }
}
