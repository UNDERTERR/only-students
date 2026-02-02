-- Payment Service 数据库表结构
-- 数据库: only_students_payment
-- 执行: mysql -u root -p only_students_payment < payment-service.sql

USE only_students_payment;

-- 支付订单表
CREATE TABLE IF NOT EXISTS payment_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) UNIQUE NOT NULL COMMENT '订单号（系统生成）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type TINYINT NOT NULL COMMENT '类型：1笔记购买 2订阅创作者 3打赏',
    target_id BIGINT NOT NULL COMMENT '目标ID（笔记ID或创作者ID）',
    target_type TINYINT NOT NULL COMMENT '目标类型：1笔记 2创作者',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    platform_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '平台手续费(默认20%)',
    creator_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '创作者所得',
    status TINYINT DEFAULT 0 COMMENT '状态：0待支付 1已支付 2支付失败 3已取消 4已退款',
    pay_channel TINYINT COMMENT '支付渠道：1微信支付 2支付宝',
    pay_time DATETIME COMMENT '支付完成时间',
    third_party_no VARCHAR(100) COMMENT '第三方支付单号',
    third_party_response TEXT COMMENT '第三方支付响应',
    expire_time DATETIME COMMENT '订单过期时间（默认30分钟）',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_pay_time (pay_time),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

-- 用户钱包表
CREATE TABLE IF NOT EXISTS wallet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '可用余额',
    frozen_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额（提现中）',
    total_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计收入',
    total_withdrawal DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计提现',
    password VARCHAR(255) COMMENT '支付密码（可选）',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

-- 钱包流水表
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type TINYINT NOT NULL COMMENT '类型：1收入 2支出 3提现 4退款',
    amount DECIMAL(10,2) NOT NULL COMMENT '变动金额',
    balance DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
    related_order_no VARCHAR(64) COMMENT '关联订单号',
    related_withdrawal_no VARCHAR(64) COMMENT '关联提现单号',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包流水表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS refund_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_no VARCHAR(64) UNIQUE NOT NULL COMMENT '退款单号',
    order_no VARCHAR(64) NOT NULL COMMENT '原订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(255) COMMENT '退款原因',
    status TINYINT DEFAULT 0 COMMENT '状态：0待处理 1已退款 2拒绝',
    refund_time DATETIME COMMENT '退款时间',
    third_party_refund_no VARCHAR(100) COMMENT '第三方退款单号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';
