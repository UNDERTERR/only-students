package com.onlystudents.withdrawal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.withdrawal.entity.WithdrawalApplication;
import com.onlystudents.withdrawal.mapper.WithdrawalApplicationMapper;
import com.onlystudents.withdrawal.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    
    private final WithdrawalApplicationMapper withdrawalApplicationMapper;
    
    private static final BigDecimal MIN_WITHDRAWAL_AMOUNT = new BigDecimal("100");
    private static final BigDecimal MAX_WITHDRAWAL_AMOUNT = new BigDecimal("50000");
    
    @Override
    @Transactional
    public WithdrawalApplication applyWithdrawal(Long userId, BigDecimal amount, Integer accountType,
                                              String accountName, String accountNumber,
                                              String bankName, String branchName) {
        // 验证提现金额
        if (amount.compareTo(MIN_WITHDRAWAL_AMOUNT) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "提现金额不能少于100元");
        }
        if (amount.compareTo(MAX_WITHDRAWAL_AMOUNT) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "提现金额不能超过50000元");
        }
        
        // 检查是否有待处理的提现申请
        LambdaQueryWrapper<WithdrawalApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WithdrawalApplication::getUserId, userId);
        wrapper.eq(WithdrawalApplication::getStatus, 0);
        
        if (withdrawalApplicationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "您有正在处理的提现申请，请等待完成后再申请");
        }
        
        WithdrawalApplication application = new WithdrawalApplication();
        application.setUserId(userId);
        application.setAmount(amount);
        application.setAccountType(accountType);
        application.setAccountName(accountName);
        application.setAccountNumber(accountNumber);
        application.setBankName(bankName);
        application.setBranchName(branchName);
        application.setStatus(0);
        
        withdrawalApplicationMapper.insert(application);
        
        log.info("用户{}申请提现{}元", userId, amount);
        return application;
    }
    
    @Override
    public List<WithdrawalApplication> getWithdrawalList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<WithdrawalApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WithdrawalApplication::getUserId, userId);
        wrapper.orderByDesc(WithdrawalApplication::getCreatedAt);
        
        Page<WithdrawalApplication> pageParam = new Page<>(page, size);
        return withdrawalApplicationMapper.selectPage(pageParam, wrapper).getRecords();
    }
    
    @Override
    public List<WithdrawalApplication> getPendingWithdrawals(Integer page, Integer size) {
        LambdaQueryWrapper<WithdrawalApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WithdrawalApplication::getStatus, 0);
        wrapper.orderByDesc(WithdrawalApplication::getCreatedAt);
        
        Page<WithdrawalApplication> pageParam = new Page<>(page, size);
        return withdrawalApplicationMapper.selectPage(pageParam, wrapper).getRecords();
    }
    
    @Override
    @Transactional
    public void auditWithdrawal(Long applicationId, Long auditorId, Integer status, String auditRemark) {
        WithdrawalApplication application = withdrawalApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现申请不存在");
        }
        
        if (application.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请已被处理");
        }
        
        application.setStatus(status);
        application.setAuditorId(auditorId);
        application.setAuditRemark(auditRemark);
        application.setAuditTime(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        // 生成交易号
        if (status == 1) {
            application.setTransactionNo(generateTransactionNo());
        }
        
        withdrawalApplicationMapper.updateById(application);
        
        log.info("管理员{}审核提现申请{}，结果：{}", auditorId, applicationId, status);
    }
    
    private String generateTransactionNo() {
        return "WD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    @Override
    public WithdrawalApplication getWithdrawalDetail(Long applicationId, Long userId) {
        WithdrawalApplication application = withdrawalApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现申请不存在");
        }
        if (!application.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该提现申请");
        }
        return application;
    }
    
    @Override
    @Transactional
    public void cancelWithdrawal(Long applicationId, Long userId) {
        WithdrawalApplication application = withdrawalApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现申请不存在");
        }
        if (!application.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权取消该提现申请");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请已被处理，无法取消");
        }
        
        withdrawalApplicationMapper.deleteById(applicationId);
        
        log.info("用户{}取消提现申请{}", userId, applicationId);
    }
}
