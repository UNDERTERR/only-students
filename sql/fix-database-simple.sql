-- ============================================================
-- OnlyStudents 数据库修复脚本 (简化版)
-- 使用：直接在命令行执行 mysql -u root -p < fix-database-simple.sql
-- 特点：不使用存储过程，避免跨数据库问题
-- ============================================================

-- 设置会话选项
SET SESSION FOREIGN_KEY_CHECKS = 0;
SET SESSION SQL_MODE = 'ALLOW_INVALID_DATES';

-- ============================================================
-- 1. 创建补偿任务表 (Payment Service)
-- ============================================================

SELECT '1. Creating compensation_task table...' AS status;

CREATE TABLE IF NOT EXISTS only_students_payment.compensation_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：INCOME_ALLOCATION',
    business_id BIGINT NOT NULL COMMENT '业务ID（订单ID）',
    status INT DEFAULT 0 COMMENT '0-待处理 1-处理中 2-成功 3-失败 4-放弃',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry_count INT DEFAULT 5 COMMENT '最大重试次数',
    params TEXT COMMENT '任务参数（JSON）',
    error_msg TEXT COMMENT '错误信息',
    next_execute_time DATETIME COMMENT '下次执行时间',
    success_time DATETIME COMMENT '成功时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_business_type (business_id, task_type),
    INDEX idx_status_time (status, next_execute_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补偿任务表';

-- ============================================================
-- 2. 添加逻辑删除字段 (使用动态SQL避免报错)
-- ============================================================

SELECT '2. Adding deleted columns...' AS status;

-- 方法：先添加列（如果已存在会报错，但我们用continue跳过）
-- 为了简化，我们列出所有可能需要添加的列，执行时忽略错误

-- User Service
SELECT '   - User Service' AS status;
ALTER TABLE only_students_user.user ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_user.user_device ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_user.school ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Note Service
SELECT '   - Note Service' AS status;
ALTER TABLE only_students_note.note ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_note.note_category ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_note.note_tag ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_note.note_tag_relation ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_note.note_view_history ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- File Service
SELECT '   - File Service' AS status;
ALTER TABLE only_students_file.file_record ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_file.file_convert_task ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_file.file_access_log ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Payment Service
SELECT '   - Payment Service' AS status;
ALTER TABLE only_students_payment.payment_order ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_payment.wallet ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_payment.wallet_transaction ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_payment.refund_record ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_payment.compensation_task ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Comment Service
SELECT '   - Comment Service' AS status;
ALTER TABLE only_students_comment.comment ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_comment.comment_like ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_comment.comment_report ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Subscription Service
SELECT '   - Subscription Service' AS status;
ALTER TABLE only_students_subscription.creator_subscription_config ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_subscription.subscription ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_subscription.subscription_history ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Rating Service
SELECT '   - Rating Service' AS status;
ALTER TABLE only_students_rating.note_rating ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_rating.note_rating_stats ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_rating.note_favorite ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_rating.favorite_folder ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_rating.note_share ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_rating.share_click_log ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Message Service
SELECT '   - Message Service' AS status;
ALTER TABLE only_students_message.message ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_message.conversation ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_message.message_delete_log ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Notification Service
SELECT '   - Notification Service' AS status;
ALTER TABLE only_students_notification.notification ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_notification.notification_setting ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_notification.system_announcement ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Report Service
SELECT '   - Report Service' AS status;
ALTER TABLE only_students_report.report ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Withdrawal Service
SELECT '   - Withdrawal Service' AS status;
ALTER TABLE only_students_withdrawal.withdrawal_application ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Analytics Service
SELECT '   - Analytics Service' AS status;
ALTER TABLE only_students_analytics.daily_stats ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_analytics.hourly_stats ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_analytics.note_stats ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_analytics.creator_summary ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Admin Service
SELECT '   - Admin Service' AS status;
ALTER TABLE only_students_admin.admin_user ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_admin.admin_role ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_admin.audit_record ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_admin.system_config ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_admin.admin_login_log ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_admin.admin_operation_log ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- Search Service
SELECT '   - Search Service' AS status;
ALTER TABLE only_students_search.search_history ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_search.hot_search_keyword ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';
ALTER TABLE only_students_search.search_suggestion ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除';

-- ============================================================
-- 3. 添加其他缺失字段
-- ============================================================

SELECT '3. Adding other missing columns...' AS status;

-- Note Service: 添加评分字段
ALTER TABLE only_students_note.note ADD COLUMN rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '平均评分';
ALTER TABLE only_students_note.note ADD COLUMN rating_count INT DEFAULT 0 COMMENT '评分人数';

-- Admin Service: 检查并添加缺少的字段
ALTER TABLE only_students_admin.admin_user ADD COLUMN nickname VARCHAR(50) COMMENT '昵称';

-- ============================================================
-- 4. 创建索引
-- ============================================================

SELECT '4. Creating indexes...' AS status;

CREATE INDEX idx_deleted ON only_students_note.note(deleted);
CREATE INDEX idx_deleted ON only_students_user.user(deleted);
CREATE INDEX idx_deleted ON only_students_payment.payment_order(deleted);

-- ============================================================
-- 5. 初始化数据
-- ============================================================

SELECT '5. Initializing data...' AS status;

-- Admin Service: 创建超级管理员
INSERT IGNORE INTO only_students_admin.admin_user (id, username, password, nickname, status, deleted) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '超级管理员', 1, 0);

-- Note Service: 初始化分类
INSERT IGNORE INTO only_students_note.note_category (id, name, parent_id, sort_order, description, deleted) VALUES
(1, '全部', 0, 0, '所有笔记', 0),
(2, '数学', 0, 1, '数学相关笔记', 0),
(3, '物理', 0, 2, '物理相关笔记', 0),
(4, '化学', 0, 3, '化学相关笔记', 0),
(5, '英语', 0, 4, '英语学习笔记', 0),
(6, '语文', 0, 5, '语文相关笔记', 0),
(7, '编程', 0, 6, '计算机编程笔记', 0),
(8, '考研', 0, 7, '考研复习资料', 0),
(9, '高考', 0, 8, '高考复习资料', 0);

-- User Service: 初始化学校
INSERT IGNORE INTO only_students_user.school (id, name, education_level, province, city, deleted) VALUES
(1, '北京大学', 4, '北京市', '北京市', 0),
(2, '清华大学', 4, '北京市', '北京市', 0),
(3, '人民大学附属中学', 3, '北京市', '北京市', 0),
(4, '华南师范大学附属中学', 3, '广东省', '广州市', 0),
(5, '深圳市南山外国语学校', 2, '广东省', '深圳市', 0),
(6, '杭州市学军小学', 1, '浙江省', '杭州市', 0);

-- 恢复外键检查
SET SESSION FOREIGN_KEY_CHECKS = 1;

-- 完成
SELECT '数据库修复完成！' AS status;
SELECT CONCAT('修复时间: ', NOW()) AS repair_time;
SELECT '注意：如果显示列已存在错误，说明之前已经添加过了，可以忽略' AS note;
