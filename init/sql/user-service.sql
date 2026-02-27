-- User Service 数据库表结构
-- 数据库: only_students_user
-- 执行: mysql -u root -p only_students_user < user-service.sql

USE only_students_user;

-- 学校信息表（基础数据）
CREATE TABLE IF NOT EXISTS school
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL COMMENT '学校名称',
    province        VARCHAR(50) COMMENT '省份',
    city            VARCHAR(50) COMMENT '城市',
    population      INT      DEFAULT 0 COMMENT '用户数量',
    status          TINYINT  DEFAULT 1 COMMENT '状态：0禁用 1正常',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学校表';

-- 用户主表
CREATE TABLE IF NOT EXISTS user
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    password        VARCHAR(255)       NOT NULL COMMENT '加密密码（BCrypt）',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '手机号',
    nickname        VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar          VARCHAR(500) COMMENT '头像URL',
    bio             TEXT COMMENT '个人简介',
    education_level TINYINT COMMENT '学段：1小学 2初中 3高中 4大学 5硕士 6博士',
    school_id       BIGINT COMMENT '学校ID',
    school_name     VARCHAR(100) COMMENT '学校名称（可选填）',
    is_creator      TINYINT  DEFAULT 0 COMMENT '是否创作者：0否 1是',
    creator_config  JSON COMMENT '创作者配置JSON（订阅价格、权益等）',
    status          TINYINT  DEFAULT 1 COMMENT '状态：0禁用 1正常',
    follower_count  INT      DEFAULT 0 COMMENT '粉丝数量',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_education_level (education_level),
    INDEX idx_school_id (school_id),
    INDEX idx_is_creator (is_creator),
    INDEX idx_status (status),
    INDEX idx_nickname (nickname),
    CONSTRAINT fk_user_school FOREIGN KEY (school_id) REFERENCES school (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 用户设备表（多端登录管理，最多3设备）
CREATE TABLE IF NOT EXISTS user_device
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL COMMENT '用户ID',
    device_id        VARCHAR(100) NOT NULL COMMENT '设备标识',
    device_type      TINYINT COMMENT '设备类型：1Web 2Mobile 3PC 4小程序',
    device_name      VARCHAR(100) COMMENT '设备名称',
    login_time       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    last_active_time DATETIME COMMENT '最后活跃时间',
    token            VARCHAR(500) COMMENT 'JWT Token',
    ip               VARCHAR(50) COMMENT 'IP地址',
    status           TINYINT  DEFAULT 1 COMMENT '状态：0已登出 1正常',
    UNIQUE KEY uk_user_device (user_id, device_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户设备表';
