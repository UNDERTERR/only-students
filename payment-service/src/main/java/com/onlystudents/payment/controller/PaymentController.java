package com.onlystudents.payment.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.payment.dto.CreateOrderRequest;
import com.onlystudents.payment.dto.OrderDTO;
import com.onlystudents.payment.dto.PayCallbackRequest;
import com.onlystudents.payment.dto.WalletDTO;
import com.onlystudents.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "订单创建/支付回调/钱包查询等接口")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/order")
    @Operation(summary = "创建订单", description = "创建支付订单")
    public Result<OrderDTO> createOrder(@RequestBody CreateOrderRequest request,
                                         @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(paymentService.createOrder(request, userId));
    }
    
    @GetMapping("/order/{orderNo}")
    @Operation(summary = "查询订单", description = "根据订单号查询订单详情")
    public Result<OrderDTO> getOrder(@PathVariable String orderNo) {
        return Result.success(paymentService.getOrder(orderNo));
    }
    
    @GetMapping("/my-orders")
    @Operation(summary = "我的订单", description = "获取我的所有订单")
    public Result<List<OrderDTO>> getMyOrders(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(paymentService.getMyOrders(userId));
    }
    
    @PostMapping("/callback")
    @Operation(summary = "支付回调", description = "接收第三方支付回调")
    public Result<Void> payCallback(@RequestBody PayCallbackRequest request) {
        paymentService.handlePayCallback(request);
        return Result.success();
    }
    
    @GetMapping("/wallet")
    @Operation(summary = "我的钱包", description = "获取钱包余额和收入统计")
    public Result<WalletDTO> getWallet(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(paymentService.getWallet(userId));
    }
    
    @GetMapping("/check-purchased/{noteId}")
    @Operation(summary = "检查是否已购买笔记", description = "检查当前用户是否已购买指定笔记")
    public Result<Boolean> checkPurchased(@PathVariable Long noteId,
                                          @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(paymentService.checkNotePurchased(userId, noteId));
    }
    
    @GetMapping("/creator/{creatorId}/revenue")
    @Operation(summary = "获取创作者收入", description = "获取创作者的总收入（已支付订单）")
    public Result<Long> getCreatorRevenue(@PathVariable Long creatorId) {
        return Result.success(paymentService.getCreatorRevenue(creatorId));
    }
}
