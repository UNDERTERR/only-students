-- Rating Service 数据库表结构
-- 数据库: only_students_rating
-- 执行: mysql -u root -p only_students_rating < rating-service.sql

USE only_students_rating;

-- 笔记评分表
CREATE TABLE IF NOT EXISTS note_rating (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    score TINYINT NOT NULL COMMENT '评分：1-5星',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_note_user (note_id, user_id),
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记评分表';

-- 笔记评分统计表（冗余统计，加速查询）
CREATE TABLE IF NOT EXISTS note_rating_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT UNIQUE NOT NULL COMMENT '笔记ID',
    avg_score DECIMAL(2,1) DEFAULT 5.0 COMMENT '平均评分',
    total_count INT DEFAULT 0 COMMENT '总评分人数',
    score_1_count INT DEFAULT 0 COMMENT '1星人数',
    score_2_count INT DEFAULT 0 COMMENT '2星人数',
    score_3_count INT DEFAULT 0 COMMENT '3星人数',
    score_4_count INT DEFAULT 0 COMMENT '4星人数',
    score_5_count INT DEFAULT 0 COMMENT '5星人数',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_note_id (note_id),
    INDEX idx_avg_score (avg_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记评分统计表';

-- 笔记收藏表
CREATE TABLE IF NOT EXISTS note_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_id BIGINT DEFAULT 0 COMMENT '收藏夹ID（0为默认收藏夹）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_note_user (note_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记收藏表';

-- 收藏夹表
CREATE TABLE IF NOT EXISTS favorite_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '收藏夹名称',
    description VARCHAR(255) COMMENT '描述',
    count INT DEFAULT 0 COMMENT '收藏数量',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认收藏夹：0否 1是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 笔记分享表（记录分享行为）
CREATE TABLE IF NOT EXISTS note_share (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT COMMENT '分享者ID（可为空，表示匿名分享）',
    share_type TINYINT NOT NULL COMMENT '分享类型：1站内 2微信 3QQ 4复制链接 5其他',
    share_code VARCHAR(64) UNIQUE COMMENT '分享短码（用于短链接）',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_share_code (share_code),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记分享表';

-- 分享点击记录表（统计用）
CREATE TABLE IF NOT EXISTS share_click_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    share_id BIGINT NOT NULL COMMENT '分享记录ID',
    click_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    referrer VARCHAR(500) COMMENT '来源页面',
    INDEX idx_share_id (share_id),
    INDEX idx_click_time (click_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享点击记录表';
