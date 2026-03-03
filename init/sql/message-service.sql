-- Message Service 数据库表结构
-- 数据库: only_students_message
-- 执行: mysql -u root -p only_students_message < message-service.sql

USE only_students_message;

-- 会话表（每个对话只在表中存一条记录）
CREATE TABLE IF NOT EXISTS conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id_1 BIGINT NOT NULL COMMENT '用户1ID（较小值）',
    user_id_2 BIGINT NOT NULL COMMENT '用户2ID（较大值）',
    last_message_id BIGINT COMMENT '最后一条消息ID',
    last_message_time DATETIME COMMENT '最后消息时间',
    last_message_preview VARCHAR(200) COMMENT '最后消息预览',
    user_1_unread_count INT DEFAULT 0 COMMENT '用户1未读数',
    user_2_unread_count INT DEFAULT 0 COMMENT '用户2未读数',
    user_1_hidden TINYINT DEFAULT 0 COMMENT '用户1是否删除会话：0否 1是',
    user_2_hidden TINYINT DEFAULT 0 COMMENT '用户2是否删除会话：0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users (user_id_1, user_id_2),
    INDEX idx_user1_time (user_id_1, last_message_time),
    INDEX idx_user2_time (user_id_2, last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    content_type TINYINT DEFAULT 1 COMMENT '内容类型：1文本 2图片 3文件',
    file_url VARCHAR(500) COMMENT '文件URL（图片/文件类型）',
    status TINYINT DEFAULT 0 COMMENT '状态：0正常 1已撤回',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation (conversation_id, created_at),
    INDEX idx_sender (sender_id, created_at),
    INDEX idx_receiver (receiver_id, created_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 消息删除记录（用户删除自己的消息视图）
CREATE TABLE IF NOT EXISTS message_delete_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '删除者ID',
    deleted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_user (message_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息删除记录表';
