-- Report Service 数据库表结构
-- 数据库: only_students_report
-- 执行: mysql -u root -p only_students_report < report-service.sql

USE only_students_report;

-- 举报表
CREATE TABLE IF NOT EXISTS report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL COMMENT '举报者ID',
    target_type TINYINT NOT NULL COMMENT '目标类型：1笔记 2用户 3评论',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    target_user_id BIGINT NOT NULL COMMENT '被举报者ID',
    reason_type TINYINT NOT NULL COMMENT '原因类型：1垃圾广告 2色情低俗 3违法违规 4侵权抄袭 5人身攻击 6其他',
    reason_detail TEXT COMMENT '详细描述',
    evidence_urls JSON COMMENT '证据图片URL数组',
    status TINYINT DEFAULT 0 COMMENT '状态：0待处理 1受理中 2已处理 3已驳回',
    process_result TINYINT COMMENT '处理结果：1警告 2删除内容 3封号 4封禁 5无违规',
    process_remark TEXT COMMENT '处理备注',
    process_time DATETIME COMMENT '处理时间',
    processor_id BIGINT COMMENT '处理人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reporter (reporter_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_target_user (target_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- 举报处理历史表
CREATE TABLE IF NOT EXISTS report_process_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL COMMENT '举报ID',
    action_type TINYINT NOT NULL COMMENT '操作类型：1受理 2转交 3处理 4驳回',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    remark TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报处理历史表';

-- 用户违规记录表
CREATE TABLE IF NOT EXISTS user_violation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    violation_type TINYINT NOT NULL COMMENT '违规类型',
    violation_desc TEXT COMMENT '违规描述',
    report_id BIGINT COMMENT '关联举报ID',
    penalty_type TINYINT COMMENT '处罚类型：1警告 2限制功能 3禁言 4封号',
    penalty_duration INT COMMENT '处罚时长（小时，0为永久）',
    penalty_start_time DATETIME COMMENT '处罚开始时间',
    penalty_end_time DATETIME COMMENT '处罚结束时间',
    is_active TINYINT DEFAULT 1 COMMENT '处罚是否有效：0已解除 1有效',
    created_by BIGINT COMMENT '处理人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_active (is_active),
    INDEX idx_penalty_time (penalty_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户违规记录表';

-- 敏感词表（用于自动审核）
CREATE TABLE IF NOT EXISTS sensitive_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL COMMENT '敏感词',
    category TINYINT DEFAULT 1 COMMENT '分类：1政治 2色情 3暴力 4广告 5其他',
    level TINYINT DEFAULT 1 COMMENT '等级：1提示 2警告 3禁止',
    is_regex TINYINT DEFAULT 0 COMMENT '是否正则：0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_word (word),
    INDEX idx_category (category),
    INDEX idx_level (level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';
