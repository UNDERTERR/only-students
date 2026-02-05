package com.onlystudents.withdrawal.service;

import com.onlystudents.withdrawal.entity.WithdrawalApplication;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalService {
    
    WithdrawalApplication applyWithdrawal(Long userId, BigDecimal amount, Integer accountType, 
                                        String accountName, String accountNumber, 
                                        String bankName, String branchName);
    
    List<WithdrawalApplication> getWithdrawalList(Long userId, Integer page, Integer size);
    
    List<WithdrawalApplication> getPendingWithdrawals(Integer page, Integer size);
    
    void auditWithdrawal(Long applicationId, Long auditorId, Integer status, String auditRemark);
    
    WithdrawalApplication getWithdrawalDetail(Long applicationId, Long userId);
    
    void cancelWithdrawal(Long applicationId, Long userId);
}
