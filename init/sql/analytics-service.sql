
USE only_students_analytics;

-- 每日统计数据表（创作者维度）
CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT NOT NULL COMMENT '创作者ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    new_views INT DEFAULT 0 COMMENT '新增浏览量',
    new_favorites INT DEFAULT 0 COMMENT '新增收藏数',
    new_comments INT DEFAULT 0 COMMENT '新增评论数',
    new_shares INT DEFAULT 0 COMMENT '新增分享数',
    new_subscribers INT DEFAULT 0 COMMENT '新增订阅者',
    lost_subscribers INT DEFAULT 0 COMMENT '流失订阅者',
    order_count INT DEFAULT 0 COMMENT '订单数',
    order_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
    income_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '当日收入（扣除平台费后）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_creator_date (creator_id, stat_date),
    INDEX idx_stat_date (stat_date),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日统计数据表';

-- 创作者累计数据表（快速查询）
CREATE TABLE IF NOT EXISTS creator_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT UNIQUE NOT NULL COMMENT '创作者ID',
    total_notes BIGINT DEFAULT 0 COMMENT '总笔记数',
    total_views BIGINT DEFAULT 0 COMMENT '总浏览量',
    total_comments BIGINT DEFAULT 0 COMMENT '总评论数',
    total_favorites BIGINT DEFAULT 0 COMMENT '总收藏数',
    total_ratings BIGINT DEFAULT 0 COMMENT '总评分次数',
    avg_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分',
    avg_heat_score DECIMAL(10,2) DEFAULT 0.00 COMMENT '平均热度分',
    weekly_ranking INT DEFAULT 0 COMMENT '周榜排名',
    monthly_ranking INT DEFAULT 0 COMMENT '月榜排名',
    -- 以下为保留字段
    total_shares INT DEFAULT 0 COMMENT '总分享数',
    total_subscribers INT DEFAULT 0 COMMENT '总订阅者数',
    today_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '今日收入',
    week_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '近7日收入',
    month_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '近30日收入',
    year_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '近一年收入',
    total_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计收入',
    last_calculated_at DATETIME COMMENT '上次计算时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_creator_id (creator_id),
    INDEX idx_total_income (total_income),
    INDEX idx_weekly_ranking (weekly_ranking),
    INDEX idx_monthly_ranking (monthly_ranking)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='创作者累计数据表';

-- 笔记统计数据表
CREATE TABLE IF NOT EXISTS note_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT UNIQUE NOT NULL COMMENT '笔记ID',
    creator_id BIGINT NOT NULL COMMENT '创作者ID',
    total_views INT DEFAULT 0 COMMENT '总浏览量',
    total_favorites INT DEFAULT 0 COMMENT '总收藏数',
    total_comments INT DEFAULT 0 COMMENT '总评论数',
    total_shares INT DEFAULT 0 COMMENT '总分享数',
    avg_reading_time INT DEFAULT 0 COMMENT '平均阅读时长（秒）',
    completion_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '读完率',
    hot_score DOUBLE DEFAULT 0 COMMENT '热度分',
    last_calculated_at DATETIME COMMENT '上次计算时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_note_id (note_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_hot_score (hot_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记统计数据表';

-- 小时级统计数据（用于实时折线图）
CREATE TABLE IF NOT EXISTS hourly_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT NOT NULL COMMENT '创作者ID',
    stat_hour DATETIME NOT NULL COMMENT '统计小时（yyyy-MM-dd HH:00:00）',
    views INT DEFAULT 0 COMMENT '浏览量',
    favorites INT DEFAULT 0 COMMENT '收藏数',
    comments INT DEFAULT 0 COMMENT '评论数',
    income DECIMAL(10,2) DEFAULT 0.00 COMMENT '收入',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_creator_hour (creator_id, stat_hour),
    INDEX idx_stat_hour (stat_hour)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小时级统计数据表';
