-- Admin Service 数据库表结构
-- 数据库: only_students_admin
-- 执行: mysql -u root -p only_students_admin < admin-service.sql

USE only_students_admin;

-- 管理员角色表
CREATE TABLE IF NOT EXISTS admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    permissions JSON COMMENT '权限列表（菜单+按钮）',
    data_scope TINYINT DEFAULT 1 COMMENT '数据范围：1全部 2本部门 3本部门及下级 4仅本人',
    description VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员角色表';

-- 管理员表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(500) COMMENT '头像',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT DEFAULT 0 COMMENT '部门ID',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_status (status),
    CONSTRAINT fk_admin_role FOREIGN KEY (role_id) REFERENCES admin_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 管理员登录日志
CREATE TABLE IF NOT EXISTS admin_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    login_ip VARCHAR(50) COMMENT '登录IP',
    login_location VARCHAR(100) COMMENT '登录地点',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    status TINYINT DEFAULT 1 COMMENT '状态：0失败 1成功',
    message VARCHAR(255) COMMENT '消息',
    INDEX idx_admin_id (admin_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员登录日志表';

-- 管理员操作日志
CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT COMMENT '管理员ID',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_desc TEXT COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip VARCHAR(50) COMMENT 'IP地址',
    execution_time INT COMMENT '执行时长（毫秒）',
    status TINYINT DEFAULT 1 COMMENT '状态：0失败 1成功',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 内容审核记录表
CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type TINYINT NOT NULL COMMENT '目标类型：1笔记 2评论 3用户资料',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    submitter_id BIGINT NOT NULL COMMENT '提交者ID',
    content_snapshot TEXT COMMENT '内容快照',
    audit_type TINYINT DEFAULT 1 COMMENT '审核类型：1自动 2人工',
    audit_status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1通过 2拒绝 3需复核',
    auto_check_result JSON COMMENT '自动审核结果',
    sensitive_words JSON COMMENT '检测到的敏感词',
    audit_remark TEXT COMMENT '审核备注',
    auditor_id BIGINT COMMENT '审核员ID',
    audited_at DATETIME COMMENT '审核时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_id),
    INDEX idx_status (audit_status),
    INDEX idx_auditor (auditor_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'string' COMMENT '类型：string/int/boolean/json',
    description VARCHAR(255) COMMENT '描述',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开：0否 1是',
    updated_by BIGINT COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 初始化角色数据
INSERT INTO admin_role (name, code, permissions, data_scope, description) VALUES
('超级管理员', 'super_admin', '["*"]', 1, '拥有所有权限'),
('运营管理员', 'operation_admin', '["user:view","note:view","note:audit","report:handle","announcement:manage"]', 1, '负责日常运营和内容审核'),
('财务管理员', 'finance_admin', '["payment:view","withdrawal:audit","analytics:view"]', 1, '负责财务相关操作'),
('客服专员', 'customer_service', '["user:view","report:view","message:reply"]', 4, '处理用户反馈和举报');
