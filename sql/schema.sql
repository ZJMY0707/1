-- ============================================================
-- 学生个人知识库与日程标注系统 - 数据库建表脚本
-- 数据库: student_knowledge_db   字符集: utf8mb4
-- ============================================================

DROP DATABASE IF EXISTS student_knowledge_db;
CREATE DATABASE student_knowledge_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_knowledge_db;

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
CREATE TABLE t_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL COMMENT '学号或用户名',
    email       VARCHAR(100) NOT NULL COMMENT '邮箱',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    role        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT / ADMIN',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_email (email)
) ENGINE=InnoDB COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 知识分类表（支持树形结构）
-- -----------------------------------------------------------
CREATE TABLE t_category (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id   BIGINT       DEFAULT NULL COMMENT '父分类ID, NULL为顶级',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES t_category(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='知识分类表（文件树）';

-- -----------------------------------------------------------
-- 3. 标签表
-- -----------------------------------------------------------
CREATE TABLE t_tag (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL COMMENT '标签名',
    color       VARCHAR(20)  DEFAULT '#1890ff' COMMENT '标签颜色',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    UNIQUE INDEX uk_user_tag (user_id, name),
    CONSTRAINT fk_tag_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='标签表';

-- -----------------------------------------------------------
-- 4. 知识条目表
-- -----------------------------------------------------------
CREATE TABLE t_knowledge_entry (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content     TEXT         COMMENT '笔记正文(Markdown)',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    category_id BIGINT       DEFAULT NULL COMMENT '所属分类',
    is_pinned   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否置顶',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_category (category_id),
    INDEX idx_created (created_at),
    CONSTRAINT fk_entry_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_entry_category FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='知识条目表';

-- -----------------------------------------------------------
-- 5. 知识条目-标签 关联表（多对多）
-- -----------------------------------------------------------
CREATE TABLE t_knowledge_tag (
    entry_id    BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    PRIMARY KEY (entry_id, tag_id),
    CONSTRAINT fk_kt_entry FOREIGN KEY (entry_id) REFERENCES t_knowledge_entry(id) ON DELETE CASCADE,
    CONSTRAINT fk_kt_tag   FOREIGN KEY (tag_id)   REFERENCES t_tag(id)             ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='知识-标签关联表';

-- -----------------------------------------------------------
-- 6. 日程任务表
-- -----------------------------------------------------------
CREATE TABLE t_task (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT         COMMENT '任务描述',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/DONE/OVERDUE',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/URGENT',
    start_time  DATETIME     DEFAULT NULL COMMENT '开始时间',
    deadline    DATETIME     NOT NULL COMMENT '截止时间',
    completed_at DATETIME    DEFAULT NULL COMMENT '完成时间',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_deadline (deadline),
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='日程任务表';
