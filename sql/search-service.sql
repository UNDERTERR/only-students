-- Search Service 数据库表结构
-- 数据库: only_students_search
-- 执行: mysql -u root -p only_students_search < search-service.sql

USE only_students_search;

-- 搜索历史表（用户维度）
CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID（未登录为NULL）',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    search_type TINYINT DEFAULT 1 COMMENT '搜索类型：1全部 2笔记 3用户',
    filters JSON COMMENT '筛选条件JSON',
    result_count INT DEFAULT 0 COMMENT '结果数量',
    ip VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_keyword (keyword),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- 热门搜索词表
CREATE TABLE IF NOT EXISTS hot_search_keyword (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(200) NOT NULL COMMENT '关键词',
    search_count INT DEFAULT 1 COMMENT '搜索次数',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    trend TINYINT DEFAULT 0 COMMENT '趋势：0持平 1上升 2下降',
    is_recommended TINYINT DEFAULT 0 COMMENT '是否推荐：0否 1是',
    category VARCHAR(50) COMMENT '分类标签',
    last_searched_at DATETIME COMMENT '最后搜索时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_keyword (keyword),
    INDEX idx_search_count (search_count),
    INDEX idx_is_recommended (is_recommended)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门搜索词表';

-- 搜索推荐表（人工配置）
CREATE TABLE IF NOT EXISTS search_suggestion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(200) NOT NULL COMMENT '关键词',
    suggestion_type TINYINT DEFAULT 1 COMMENT '类型：1搜索建议 2热门推荐 3相关推荐',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_by BIGINT COMMENT '创建者',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_suggestion_type (suggestion_type),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索推荐表';
