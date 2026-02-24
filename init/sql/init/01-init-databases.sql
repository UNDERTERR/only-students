-- OnlyStudents 数据库初始化脚本
-- 创建各个微服务数据库
-- 执行: mysql -u root -p < init-databases.sql

-- 创建用户服务数据库
CREATE DATABASE IF NOT EXISTS only_students_user 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建笔记服务数据库
CREATE DATABASE IF NOT EXISTS only_students_note 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建文件服务数据库
CREATE DATABASE IF NOT EXISTS only_students_file 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建订阅服务数据库
CREATE DATABASE IF NOT EXISTS only_students_subscription 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建支付服务数据库
CREATE DATABASE IF NOT EXISTS only_students_payment 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建提现服务数据库
CREATE DATABASE IF NOT EXISTS only_students_withdrawal 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建评论服务数据库
CREATE DATABASE IF NOT EXISTS only_students_comment 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建评分服务数据库
CREATE DATABASE IF NOT EXISTS only_students_rating 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建消息服务数据库
CREATE DATABASE IF NOT EXISTS only_students_message 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建通知服务数据库
CREATE DATABASE IF NOT EXISTS only_students_notification 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建举报服务数据库
CREATE DATABASE IF NOT EXISTS only_students_report 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建数据分析服务数据库
CREATE DATABASE IF NOT EXISTS only_students_analytics 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建后台管理服务数据库
CREATE DATABASE IF NOT EXISTS only_students_admin 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建搜索服务数据库
CREATE DATABASE IF NOT EXISTS only_students_search 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 显示所有创建的数据库
SHOW DATABASES LIKE 'only_students_%';
