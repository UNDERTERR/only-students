-- Withdrawal Service 数据库表结构
-- 数据库: only_students_withdrawal
-- 执行: mysql -u root -p only_students_withdrawal < withdrawal-service.sql

USE only_students_withdrawal;

-- 提现申请表
CREATE TABLE IF NOT EXISTS withdrawal_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_no VARCHAR(64) UNIQUE NOT NULL COMMENT '申请单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '手续费',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实际到账金额',
    channel TINYINT NOT NULL COMMENT '渠道：1微信零钱 2支付宝余额',
    account_info VARCHAR(255) NOT NULL COMMENT '收款账号信息',
    real_name VARCHAR(50) COMMENT '真实姓名',
    status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1审核通过 2审核拒绝 3处理中 4已打款 5打款失败',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    fail_reason VARCHAR(255) COMMENT '失败原因',
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    audited_at DATETIME COMMENT '审核时间',
    auditor_id BIGINT COMMENT '审核员ID',
    processed_at DATETIME COMMENT '处理时间',
    completed_at DATETIME COMMENT '完成时间',
    third_party_no VARCHAR(100) COMMENT '第三方流水号',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_applied_at (applied_at),
    INDEX idx_application_no (application_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现申请表';

-- 提现配置表
CREATE TABLE IF NOT EXISTS withdrawal_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    min_amount DECIMAL(10,2) DEFAULT 1.00 COMMENT '最低提现金额',
    max_amount DECIMAL(10,2) DEFAULT 50000.00 COMMENT '最高提现金额',
    fee_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '手续费率',
    fee_fixed DECIMAL(10,2) DEFAULT 0.00 COMMENT '固定手续费',
    audit_required TINYINT DEFAULT 1 COMMENT '是否需要审核：0否 1是',
    auto_audit_threshold DECIMAL(10,2) DEFAULT 1000.00 COMMENT '自动审核阈值（低于此金额自动通过）',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现配置表';

-- 初始化提现配置
INSERT INTO withdrawal_config (min_amount, max_amount, fee_rate, fee_fixed) VALUES
(1.00, 50000.00, 0.0000, 0.00);
