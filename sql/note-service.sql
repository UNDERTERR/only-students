-- Note Service 数据库表结构
-- 数据库: only_students_note
-- 执行: mysql -u root -p only_students_note < note-service.sql

USE only_students_note;

-- 笔记分类表
CREATE TABLE IF NOT EXISTS note_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID（0为顶级）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(200) COMMENT '图标URL',
    description VARCHAR(255) COMMENT '分类描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记分类表';

-- 笔记标签表
CREATE TABLE IF NOT EXISTS note_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name),
    INDEX idx_usage_count (usage_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签表';

-- 笔记主表
CREATE TABLE IF NOT EXISTS note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '创作者ID',
    auther_username VARCHAR(50) COMMENT '作者用户名',
    auther_nickname VARCHAR(50) COMMENT '作者昵称',
    auther_avatar VARCHAR(500) COMMENT '作者头像',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容摘要',
    cover_image VARCHAR(500) COMMENT '封面图URL',
    category_id BIGINT COMMENT '分类ID',
    visibility TINYINT DEFAULT 0 COMMENT '可见性：0公开 1仅订阅可见 2仅付费可见 3订阅后付费可见 4仅自己可见',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格（付费笔记）',
    original_file_id BIGINT COMMENT '原文件ID（file-service）',
    pdf_file_id BIGINT COMMENT 'PDF文件ID（预览用）',
    status TINYINT DEFAULT 0 COMMENT '状态：0草稿 1审核中 2已通过 3已拒绝 4已删除',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    share_count INT DEFAULT 0 COMMENT '分享数',
    hot_score DOUBLE DEFAULT 0 COMMENT '热度分',
    education_level TINYINT COMMENT '适用学段：1小学 2初中 3高中 4大学 5研究生',
    tags JSON COMMENT '笔记标签列表',
    school_id BIGINT COMMENT '适用学校ID（可选）',
    school_name VARCHAR(100) COMMENT '适用学校名称',
    subject VARCHAR(50) COMMENT '学科：数学/物理/化学等',
    publish_time DATETIME COMMENT '发布时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_auther_username (auther_username),
    INDEX idx_auther_nickname (auther_nickname),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_visibility (visibility),
    INDEX idx_hot_score (hot_score),
    INDEX idx_publish_time (publish_time),
    INDEX idx_education_level (education_level),
    INDEX idx_school_id (school_id),
    INDEX idx_subject (subject),
    INDEX idx_price (price),
    FULLTEXT INDEX ft_title (title) WITH PARSER ngram,
    CONSTRAINT fk_note_category FOREIGN KEY (category_id) REFERENCES note_category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- 笔记标签关联表
CREATE TABLE IF NOT EXISTS note_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_note_tag (note_id, tag_id),
    INDEX idx_note_id (note_id),
    INDEX idx_tag_id (tag_id),
    CONSTRAINT fk_relation_note FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE,
    CONSTRAINT fk_relation_tag FOREIGN KEY (tag_id) REFERENCES note_tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签关联表';

-- 笔记浏览历史（用于推荐和统计）
CREATE TABLE IF NOT EXISTS note_view_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT COMMENT '浏览者ID（未登录为NULL）',
    view_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_view_time (view_time),
    CONSTRAINT fk_history_note FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记浏览历史表';

-- 初始化分类数据
INSERT INTO note_category (name, parent_id, sort_order, description) VALUES
('全部', 0, 0, '所有笔记'),
('数学', 0, 1, '数学相关笔记'),
('物理', 0, 2, '物理相关笔记'),
('化学', 0, 3, '化学相关笔记'),
('英语', 0, 4, '英语学习笔记'),
('语文', 0, 5, '语文相关笔记'),
('编程', 0, 6, '计算机编程笔记'),
('考研', 0, 7, '考研复习资料'),
('高考', 0, 8, '高考复习资料');
