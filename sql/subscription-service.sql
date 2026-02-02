-- Subscription Service 数据库表结构
-- 数据库: only_students_subscription
-- 执行: mysql -u root -p only_students_subscription < subscription-service.sql

USE only_students_subscription;

-- 创作者订阅配置表
CREATE TABLE IF NOT EXISTS creator_subscription_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT UNIQUE NOT NULL COMMENT '创作者ID',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '订阅价格（可为0）',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用订阅：0否 1是',
    benefits TEXT COMMENT '订阅权益说明JSON',
    description TEXT COMMENT '订阅描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_creator_id (creator_id),
    INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='创作者订阅配置表';

-- 订阅关系表
CREATE TABLE IF NOT EXISTS subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscriber_id BIGINT NOT NULL COMMENT '订阅者ID',
    creator_id BIGINT NOT NULL COMMENT '创作者ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0已取消 1正常',
    expire_time DATETIME COMMENT '过期时间（NULL为永久有效）',
    order_id BIGINT COMMENT '关联订单ID',
    price DECIMAL(10,2) COMMENT '订阅时价格',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_subscriber_creator (subscriber_id, creator_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_subscriber_id (subscriber_id),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅关系表';

-- 订阅变更历史表
CREATE TABLE IF NOT EXISTS subscription_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL COMMENT '订阅ID',
    action_type TINYINT NOT NULL COMMENT '操作类型：1订阅 2续费 3取消',
    order_id BIGINT COMMENT '关联订单ID',
    price DECIMAL(10,2) COMMENT '金额',
    expire_time DATETIME COMMENT '新的过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_subscription_id (subscription_id),
    INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅变更历史表';
