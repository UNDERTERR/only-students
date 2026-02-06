package com.onlystudents.payment.service;

import com.onlystudents.payment.dto.WalletDTO;

import java.math.BigDecimal;

/**
 * 钱包服务
 */
public interface WalletService {
    
    /**
     * 获取钱包信息
     */
    WalletDTO getWallet(Long userId);
    
    /**
     * 创建钱包
     */
    void createWallet(Long userId);
    
    /**
     * 增加收入
     * 
     * @param userId 用户ID（创作者）
     * @param orderId 订单ID
     * @param amount 金额
     */
    void addIncome(Long userId, Long orderId, BigDecimal amount);
}
