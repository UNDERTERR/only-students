package com.onlystudents.payment.service.impl;

import com.onlystudents.payment.dto.WalletDTO;
import com.onlystudents.payment.entity.Wallet;
import com.onlystudents.payment.entity.WalletTransaction;
import com.onlystudents.payment.mapper.WalletMapper;
import com.onlystudents.payment.mapper.WalletTransactionMapper;
import com.onlystudents.payment.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 钱包服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper transactionMapper;
    
    @Override
    public WalletDTO getWallet(Long userId) {
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            // 自动创建钱包
            createWallet(userId);
            wallet = walletMapper.selectByUserId(userId);
        }
        return convertToDTO(wallet);
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
        log.info("创建钱包成功: userId={}", userId);
    }
    
    @Override
    @Transactional
    public void addIncome(Long userId, Long orderId, BigDecimal amount) {
        // 增加余额
        int rows = walletMapper.addIncome(userId, amount);
        if (rows == 0) {
            throw new RuntimeException("增加余额失败，可能钱包不存在");
        }
        
        // 记录流水
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            throw new RuntimeException("钱包不存在");
        }
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setType(1); // 收入
        transaction.setAmount(amount);
        transaction.setBalance(wallet.getBalance());
        transaction.setRelatedOrderNo(orderId.toString());
        transaction.setRemark("订单收入");
        
        transactionMapper.insert(transaction);
        log.info("增加收入成功: userId={}, orderId={}, amount={}", userId, orderId, amount);
    }
    
    private WalletDTO convertToDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        BeanUtils.copyProperties(wallet, dto);
        return dto;
    }
}
