-- Notification Service 数据库表结构
-- 数据库: only_students_notification
-- 执行: mysql -u root -p only_students_notification < notification-service.sql

USE only_students_notification;

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    type TINYINT NOT NULL COMMENT '类型：1系统 2订阅 3评论 4点赞 5收藏 6私信 7收益',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    target_id BIGINT COMMENT '关联目标ID（笔记ID/用户ID等）',
    target_type TINYINT COMMENT '目标类型：1笔记 2用户 3评论 4订单',
    extra_data JSON COMMENT '额外数据',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    read_time DATETIME COMMENT '阅读时间',
    send_channel TINYINT DEFAULT 1 COMMENT '发送渠道：1站内 2推送 3邮件',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_type (user_id, type),
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 用户通知设置表
CREATE TABLE IF NOT EXISTS notification_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    system_notify TINYINT DEFAULT 1 COMMENT '系统通知：0关闭 1开启',
    subscription_notify TINYINT DEFAULT 1 COMMENT '订阅通知',
    comment_notify TINYINT DEFAULT 1 COMMENT '评论通知',
    like_notify TINYINT DEFAULT 1 COMMENT '点赞通知',
    favorite_notify TINYINT DEFAULT 1 COMMENT '收藏通知',
    message_notify TINYINT DEFAULT 1 COMMENT '私信通知',
    income_notify TINYINT DEFAULT 1 COMMENT '收益通知',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知设置表';

-- 系统公告表（发给所有用户）
CREATE TABLE IF NOT EXISTS system_announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type TINYINT DEFAULT 1 COMMENT '类型：1普通 2重要 3紧急',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    start_time DATETIME COMMENT '开始展示时间',
    end_time DATETIME COMMENT '结束展示时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0草稿 1已发布 2已下线',
    created_by BIGINT COMMENT '创建者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_is_top (is_top),
    INDEX idx_time_range (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 评论通知表
CREATE TABLE IF NOT EXISTS comment_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL COMMENT '评论者ID',
    to_user_id BIGINT NOT NULL COMMENT '被评论者ID（笔记作者）',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_to_user (to_user_id),
    INDEX idx_to_user_read (to_user_id, is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论通知表';

-- 收藏通知表
CREATE TABLE IF NOT EXISTS favorite_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL COMMENT '收藏者ID',
    to_user_id BIGINT NOT NULL COMMENT '被收藏者ID（笔记作者）',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    favorite_id BIGINT NOT NULL COMMENT '收藏记录ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_to_user (to_user_id),
    INDEX idx_to_user_read (to_user_id, is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏通知表';

-- 私信通知表
CREATE TABLE IF NOT EXISTS message_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL COMMENT '发送者ID',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是（逻辑删除）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_to_user (to_user_id),
    INDEX idx_to_user_read (to_user_id, is_read),
    INDEX idx_conversation (conversation_id),
    INDEX idx_created_at (created_at),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信通知表';

-- 粉丝关注通知表
CREATE TABLE IF NOT EXISTS follower_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL COMMENT '关注者ID',
    to_user_id BIGINT NOT NULL COMMENT '被关注者ID',
    subscription_id BIGINT NOT NULL COMMENT '关注记录ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_to_user (to_user_id),
    INDEX idx_to_user_read (to_user_id, is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='粉丝关注通知表';
