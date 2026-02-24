-- Comment Service 数据库表结构
-- 数据库: only_students_comment
-- 执行: mysql -u root -p only_students_comment < comment-service.sql

USE only_students_comment;

-- 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID（0为顶级评论）',
    root_id BIGINT DEFAULT 0 COMMENT '根评论ID（用于楼中楼，0为顶层）',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    reply_count INT DEFAULT 0 COMMENT '回复数（子评论数量）',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 2审核中 3违规',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_root_id (root_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    INDEX idx_created_at (created_at),
    INDEX idx_note_user_read (note_id, user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 评论点赞表
CREATE TABLE IF NOT EXISTS comment_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comment_user (comment_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- 评论举报表（配合举报服务）
CREATE TABLE IF NOT EXISTS comment_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    reporter_id BIGINT NOT NULL COMMENT '举报者ID',
    report_id BIGINT COMMENT '关联举报记录ID（report-service）',
    status TINYINT DEFAULT 0 COMMENT '状态：0未处理 1已处理',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_id (comment_id),
    INDEX idx_reporter_id (reporter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论举报关联表';
