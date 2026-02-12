-- File Service 数据库表结构
-- 数据库: only_students_file
-- 执行: mysql -u root -p only_students_file < file-service.sql

USE only_students_file;

-- 文件记录表
CREATE TABLE IF NOT EXISTS file_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_name VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型：word/excel/ppt/pdf/image',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    md5_hash VARCHAR(64) COMMENT 'MD5校验值（用于秒传）',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    storage_type TINYINT DEFAULT 1 COMMENT '存储类型：1本地MinIO 2阿里云OSS',
    status TINYINT DEFAULT 1 COMMENT '状态：0临时 1正常 2已删除',
    access_level TINYINT DEFAULT 0 COMMENT '访问权限：0-私有 1-公开',
    expire_time DATETIME COMMENT '过期时间（临时文件）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_uploader (uploader_id),
    INDEX idx_md5 (md5_hash),
    INDEX idx_status (status),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- 文件转换任务表（Office转PDF）
CREATE TABLE IF NOT EXISTS file_convert_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_file_id BIGINT NOT NULL COMMENT '源文件ID（Office文件）',
    target_file_id BIGINT COMMENT '目标文件ID（PDF文件）',
    convert_type TINYINT DEFAULT 1 COMMENT '转换类型：1Office转PDF',
    task_status TINYINT DEFAULT 0 COMMENT '状态：0待处理 1处理中 2成功 3失败',
    error_msg TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    started_at DATETIME COMMENT '开始处理时间',
    completed_at DATETIME COMMENT '完成时间',
    INDEX idx_source_file (source_file_id),
    INDEX idx_status (task_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件转换任务表';

-- 用户文件访问记录（防盗链和统计）
CREATE TABLE IF NOT EXISTS file_access_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    user_id BIGINT COMMENT '访问者ID',
    access_type TINYINT NOT NULL COMMENT '访问类型：1预览 2下载（禁用）',
    access_token VARCHAR(255) COMMENT '临时访问Token',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    access_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    expire_time DATETIME COMMENT 'Token过期时间',
    INDEX idx_file_id (file_id),
    INDEX idx_user_id (user_id),
    INDEX idx_access_time (access_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件访问记录表';
