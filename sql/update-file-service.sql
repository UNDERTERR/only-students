-- File Service 数据库更新脚本
-- 执行: mysql -u root -p only_students_file < update-file-service.sql

USE only_students_file;

-- 添加 access_level 字段到 file_record 表
ALTER TABLE file_record 
ADD COLUMN IF NOT EXISTS access_level TINYINT DEFAULT 0 COMMENT '访问权限：0-私有 1-公开';

-- 更新已有记录的访问权限（根据文件路径判断）
-- avatars/ 和 public/ 前缀的文件设为公开
UPDATE file_record 
SET access_level = 1 
WHERE file_path LIKE 'avatars/%' OR file_path LIKE 'public/%';

-- 添加索引
ALTER TABLE file_record 
ADD INDEX idx_access_level (access_level);

-- 查看更新结果
SELECT 
    access_level,
    COUNT(*) as count
FROM file_record 
GROUP BY access_level;
