-- Subscription Service 数据库表结构
-- 数据库: only_students_subscription
-- 执行: mysql -u root -p only_students_subscription < subscription-service.sql
USE only_students_subscription;
-- 创作者订阅配置表（简化：只需记录是否启用）
CREATE TABLE IF NOT EXISTS creator_subscription_config
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT UNIQUE NOT NULL COMMENT '创作者ID',
    is_enabled TINYINT  DEFAULT 1 COMMENT '是否启用订阅：0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_creator_id (creator_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='创作者订阅配置表';
-- 订阅关系表（简化：移除价格、订单相关字段）
CREATE TABLE IF NOT EXISTS subscription
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscriber_id BIGINT NOT NULL COMMENT '订阅者ID',
    creator_id    BIGINT NOT NULL COMMENT '创作者ID',
    is_read       TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_subscriber_creator (subscriber_id, creator_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_subscriber_id (subscriber_id),
    INDEX idx_creator_read (creator_id, is_read)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订阅关系表';