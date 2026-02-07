package com.onlystudents.withdrawal.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.withdrawal.entity.WithdrawalApplication;
import com.onlystudents.withdrawal.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/withdrawal")
@RequiredArgsConstructor
@Tag(name = "提现管理", description = "申请提现、审核提现等接口")
public class WithdrawalController {
    
    private final WithdrawalService withdrawalService;
    
    @PostMapping("/apply")
    @Operation(summary = "申请提现", description = "创作者申请提现")
    public Result<WithdrawalApplication> applyWithdrawal(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                      @RequestParam(name = "amount") BigDecimal amount,
                                                      @RequestParam(name = "accountType") Integer accountType,
                                                      @RequestParam(name = "accountName") String accountName,
                                                      @RequestParam(name = "accountNumber") String accountNumber,
                                                      @RequestParam(name = "bankName", required = false) String bankName,
                                                      @RequestParam(name = "branchName", required = false) String branchName) {
        return Result.success(withdrawalService.applyWithdrawal(userId, amount, accountType, 
                                                              accountName, accountNumber, bankName, branchName));
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取提现列表", description = "获取当前用户的提现申请列表")
    public Result<List<WithdrawalApplication>> getWithdrawalList(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                               @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                               @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(withdrawalService.getWithdrawalList(userId, page, size));
    }
    
    @GetMapping("/pending")
    @Operation(summary = "获取待审核提现列表", description = "管理员获取待审核的提现申请列表")
    public Result<List<WithdrawalApplication>> getPendingWithdrawals(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                                    @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(withdrawalService.getPendingWithdrawals(page, size));
    }
    
    @PostMapping("/audit/{applicationId}")
    @Operation(summary = "审核提现", description = "管理员审核提现申请")
    public Result<Void> auditWithdrawal(@PathVariable Long applicationId,
                                       @RequestHeader(CommonConstants.USER_ID_HEADER) Long auditorId,
                                       @RequestParam(name = "status") Integer status,
                                       @RequestParam(name = "auditRemark") String auditRemark) {
        withdrawalService.auditWithdrawal(applicationId, auditorId, status, auditRemark);
        return Result.success();
    }
    
    @GetMapping("/{applicationId}")
    @Operation(summary = "获取提现详情", description = "获取指定提现申请的详细信息")
    public Result<WithdrawalApplication> getWithdrawalDetail(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                            @PathVariable Long applicationId) {
        return Result.success(withdrawalService.getWithdrawalDetail(applicationId, userId));
    }
    
    @DeleteMapping("/{applicationId}")
    @Operation(summary = "取消提现申请", description = "取消未处理的提现申请")
    public Result<Void> cancelWithdrawal(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                        @PathVariable Long applicationId) {
        withdrawalService.cancelWithdrawal(applicationId, userId);
        return Result.success();
    }
}
