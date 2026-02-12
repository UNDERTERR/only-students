package com.onlystudents.withdrawal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "申请提现请求")
public class ApplyWithdrawalRequest {
    
    @Schema(description = "提现金额")
    private BigDecimal amount;
    
    @Schema(description = "账户类型: 1-支付宝 2-微信 3-银行卡")
    private Integer accountType;
    
    @Schema(description = "账户姓名")
    private String accountName;
    
    @Schema(description = "账户号码")
    private String accountNumber;
    
    @Schema(description = "银行名称", required = false)
    private String bankName;
    
    @Schema(description = "支行名称", required = false)
    private String branchName;
}
