-- =============================================
-- 汽车研发知识共享平台 - 数据库初始化脚本
-- 数据库: car_dev_hub
-- 版本: V1.0
-- 日期: 2026-01-12
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS car_dev_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE car_dev_hub;

-- =============================================
-- 1. 用户表 (sys_user)
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    avatar VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    department VARCHAR(100) DEFAULT NULL COMMENT '部门',
    position VARCHAR(100) DEFAULT NULL COMMENT '职位',
    bio TEXT DEFAULT NULL COMMENT '个人简介',
    role VARCHAR(20) NOT NULL DEFAULT 'DEVELOPER' COMMENT '角色编码(ADMIN/EXPERT/DEVELOPER/GUEST)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0禁用/1启用）',
    login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
    lock_time DATETIME DEFAULT NULL COMMENT '锁定时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 知识资源表 (knowledge)
-- =============================================
DROP TABLE IF EXISTS knowledge;
CREATE TABLE knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    type VARCHAR(50) NOT NULL COMMENT '知识类型',
    domain VARCHAR(50) NOT NULL COMMENT '所属领域',
    summary TEXT NOT NULL COMMENT '摘要',
    content LONGTEXT DEFAULT NULL COMMENT '正文内容（Markdown）',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    dislike_count INT NOT NULL DEFAULT 0 COMMENT '踩数',
    collect_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0草稿/1待审核/2已发布/3已下架）',
    version INT NOT NULL DEFAULT 1 COMMENT '当前版本号',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_author_id (author_id),
    INDEX idx_type (type),
    INDEX idx_domain (domain),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FULLTEXT INDEX ft_title_summary (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识资源表';

-- =============================================
-- 3. 知识附件表 (knowledge_attachment)
-- =============================================
DROP TABLE IF EXISTS knowledge_attachment;
CREATE TABLE knowledge_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识ID',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_knowledge_id (knowledge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识附件表';

-- =============================================
-- 4. 知识标签表 (knowledge_tag)
-- =============================================
DROP TABLE IF EXISTS knowledge_tag;
CREATE TABLE knowledge_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识标签表';

-- =============================================
-- 5. 知识版本历史表 (knowledge_version)
-- =============================================
DROP TABLE IF EXISTS knowledge_version;
CREATE TABLE knowledge_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识ID',
    version INT NOT NULL COMMENT '版本号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary TEXT NOT NULL COMMENT '摘要',
    content LONGTEXT DEFAULT NULL COMMENT '正文内容',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_version (knowledge_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识版本历史表';

-- =============================================
-- 6. 项目表 (project)
-- =============================================
DROP TABLE IF EXISTS project;
CREATE TABLE project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT DEFAULT NULL COMMENT '项目描述',
    leader_id BIGINT NOT NULL COMMENT '负责人ID',
    start_date DATE DEFAULT NULL COMMENT '开始日期',
    end_date DATE DEFAULT NULL COMMENT '结束日期',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0规划中/1进行中/2已完成/3已归档）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_leader_id (leader_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- =============================================
-- 7. 项目成员表 (project_member)
-- =============================================
DROP TABLE IF EXISTS project_member;
CREATE TABLE project_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_project_user (project_id, user_id),
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- =============================================
-- 8. 知识-项目关联表 (knowledge_project)
-- =============================================
DROP TABLE IF EXISTS knowledge_project;
CREATE TABLE knowledge_project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_knowledge_project (knowledge_id, project_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识-项目关联表';

-- =============================================
-- 9. 讨论/问答表 (discussion)
-- =============================================
DROP TABLE IF EXISTS discussion;
CREATE TABLE discussion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    content TEXT NOT NULL COMMENT '问题内容',
    author_id BIGINT NOT NULL COMMENT '提问者ID',
    knowledge_id BIGINT DEFAULT NULL COMMENT '关联知识ID',
    project_id BIGINT DEFAULT NULL COMMENT '关联项目ID',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    reply_count INT NOT NULL DEFAULT 0 COMMENT '回复数',
    is_resolved TINYINT NOT NULL DEFAULT 0 COMMENT '是否已解决',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_author_id (author_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_project_id (project_id),
    INDEX idx_is_resolved (is_resolved),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论/问答表';

-- =============================================
-- 10. 讨论标签表 (discussion_tag)
-- =============================================
DROP TABLE IF EXISTS discussion_tag;
CREATE TABLE discussion_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    discussion_id BIGINT NOT NULL COMMENT '讨论ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    INDEX idx_discussion_id (discussion_id),
    INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论标签表';

-- =============================================
-- 11. 讨论回复表 (discussion_reply)
-- =============================================
DROP TABLE IF EXISTS discussion_reply;
CREATE TABLE discussion_reply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    discussion_id BIGINT NOT NULL COMMENT '讨论ID',
    content TEXT NOT NULL COMMENT '回复内容',
    author_id BIGINT NOT NULL COMMENT '回复者ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父回复ID（用于嵌套回复）',
    is_accepted TINYINT NOT NULL DEFAULT 0 COMMENT '是否被采纳',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_discussion_id (discussion_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论回复表';

-- =============================================
-- 12. 收藏夹表 (collect_folder)
-- =============================================
DROP TABLE IF EXISTS collect_folder;
CREATE TABLE collect_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '收藏夹名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- =============================================
-- 13. 用户收藏表 (user_collect)
-- =============================================
DROP TABLE IF EXISTS user_collect;
CREATE TABLE user_collect (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识ID',
    folder_id BIGINT DEFAULT NULL COMMENT '收藏夹ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_knowledge (user_id, knowledge_id),
    INDEX idx_user_id (user_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_folder_id (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- =============================================
-- 14. 用户点赞表 (user_like)
-- =============================================
DROP TABLE IF EXISTS user_like;
CREATE TABLE user_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型(knowledge/reply/comment)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    is_like TINYINT NOT NULL DEFAULT 1 COMMENT '1点赞 0踩',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表';

-- =============================================
-- 15. 评论表 (comment)
-- =============================================
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    knowledge_id BIGINT NOT NULL COMMENT '知识ID',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =============================================
-- 16. 搜索历史表 (search_history)
-- =============================================
DROP TABLE IF EXISTS search_history;
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- =============================================
-- 17. 系统配置表 (sys_config)
-- =============================================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =============================================
-- 18. 操作日志表 (operation_log)
-- =============================================
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    params TEXT DEFAULT NULL COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    duration BIGINT DEFAULT NULL COMMENT '执行时长(ms)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 19. 系统通知表 (sys_notification)
-- =============================================
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收方用户ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    type VARCHAR(20) NOT NULL COMMENT '通知类型(system/like/comment/audit)',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读(0未读/1已读)',
    reference_id BIGINT DEFAULT NULL COMMENT '关联的业务ID(如知识ID)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- =============================================
-- 初始数据
-- =============================================

-- 插入管理员账户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, email, department, position, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000001', 'admin@cardev.com', '信息技术部', '系统管理员', 'ADMIN', 1);

-- 插入技术专家账户 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, phone, email, department, position, role, status) VALUES
('zhangwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张伟', '13800000002', 'zhangwei@cardev.com', '动力系统部', '技术总监', 'EXPERT', 1),
('liming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李明', '13800000003', 'liming@cardev.com', '软件算法部', '首席工程师', 'EXPERT', 1);

-- 插入研发人员账户 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, phone, email, department, position, role, status) VALUES
('wangfang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王芳', '13800000004', 'wangfang@cardev.com', '电子电控部', '高级工程师', 'DEVELOPER', 1),
('liuyang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘洋', '13800000005', 'liuyang@cardev.com', '底盘工程部', '工程师', 'DEVELOPER', 1),
('chenxi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈曦', '13800000006', 'chenxi@cardev.com', '整车设计部', '工程师', 'DEVELOPER', 1);

-- 插入示例项目
INSERT INTO project (name, description, leader_id, start_date, end_date, status) VALUES
('新能源电池管理系统', '开发下一代电动汽车电池管理系统，实现更高效的能量管理和更长的电池寿命', 2, '2026-01-01', '2026-12-31', 1),
('自动驾驶感知模块', '研发L4级别自动驾驶的多传感器融合感知系统', 3, '2026-02-01', '2026-08-31', 1),
('智能座舱系统', '新一代智能座舱人机交互系统开发', 2, '2025-06-01', '2025-12-31', 2),
('底盘电控升级', '传统底盘系统电子化升级改造项目', 5, '2026-03-01', NULL, 0);

-- 插入项目成员
INSERT INTO project_member (project_id, user_id) VALUES
(1, 2), (1, 4), (1, 5),
(2, 3), (2, 4), (2, 6),
(3, 2), (3, 5), (3, 6),
(4, 5), (4, 6);

-- 插入示例知识
INSERT INTO knowledge (title, type, domain, summary, content, author_id, view_count, like_count, collect_count, status, version) VALUES
('电池管理系统(BMS)核心算法详解', '技术文档', '电子电控', '本文详细介绍了电池管理系统中SOC估算、均衡策略、热管理等核心算法的原理和实现方法。', '# 电池管理系统(BMS)核心算法详解\n\n## 1. 概述\n电池管理系统(Battery Management System, BMS)是电动汽车动力电池系统的核心组件...\n\n## 2. SOC估算算法\n### 2.1 安时积分法\n...\n\n### 2.2 卡尔曼滤波法\n...', 2, 1280, 56, 23, 2, 1),
('自动驾驶多传感器融合方案', '设计方案', '软件算法', '基于激光雷达、摄像头、毫米波雷达的多传感器融合感知方案设计文档。', '# 自动驾驶多传感器融合方案\n\n## 1. 传感器配置\n- 激光雷达: 128线...\n- 摄像头: 8路环视...\n- 毫米波雷达: 5个...\n\n## 2. 融合架构\n...', 3, 2100, 89, 45, 2, 2),
('转向系统异响故障排查指南', '故障解决方案', '底盘工程', '针对转向系统常见异响问题的诊断流程和解决方案汇总。', '# 转向系统异响故障排查指南\n\n## 常见异响类型\n1. 低速转向时的咯吱声\n2. 高速行驶方向盘抖动...\n\n## 诊断流程\n...', 5, 876, 34, 18, 2, 1),
('AUTOSAR架构详解与实践', '技术文档', '软件算法', 'AUTOSAR软件架构标准详解，包含BSW、RTE、ASW各层详细说明及实践经验。', '# AUTOSAR架构详解\n\n## 1. AUTOSAR概述\nAUTOSAR (AUTomotive Open System ARchitecture)...', 3, 1560, 72, 38, 2, 1),
('电驱系统NVH优化报告', '测试报告', '动力系统', '电驱动总成NVH测试报告及优化方案，包含振动噪声测试数据分析。', '# 电驱系统NVH优化报告\n\n## 测试概述\n测试对象：XX型号电驱动总成...\n\n## 测试结果\n...', 2, 654, 28, 12, 2, 1);

-- 插入知识标签
INSERT INTO knowledge_tag (knowledge_id, tag_name) VALUES
(1, 'BMS'), (1, 'SOC估算'), (1, '电池管理'),
(2, '自动驾驶'), (2, '传感器融合'), (2, '激光雷达'),
(3, '底盘'), (3, '转向系统'), (3, '故障诊断'),
(4, 'AUTOSAR'), (4, '软件架构'), (4, '嵌入式'),
(5, 'NVH'), (5, '电驱系统'), (5, '测试报告');

-- 插入知识-项目关联
INSERT INTO knowledge_project (knowledge_id, project_id) VALUES
(1, 1), (2, 2), (4, 2), (5, 1);

-- 插入示例讨论
INSERT INTO discussion (title, content, author_id, knowledge_id, view_count, reply_count, is_resolved) VALUES
('BMS均衡策略的选择问题', '请教各位专家，在实际项目中被动均衡和主动均衡如何选择？各有什么优缺点？', 4, 1, 156, 3, 1),
('多传感器时间同步方案', '激光雷达、摄像头、毫米波雷达的时间同步精度要求是多少？有什么好的同步方案推荐？', 6, 2, 234, 5, 0),
('AUTOSAR CP与AP的区别', '有没有大佬能详细解释一下AUTOSAR Classic Platform和Adaptive Platform的主要区别？', 4, 4, 189, 4, 1);

-- 插入讨论回复
INSERT INTO discussion_reply (discussion_id, content, author_id, is_accepted, like_count) VALUES
(1, '被动均衡成本低、结构简单，但均衡效率较低；主动均衡效率高但成本和复杂度都更高。建议根据项目成本预算和性能要求来选择。', 2, 1, 12),
(1, '补充一下，对于长续航车型，主动均衡的优势会更明显。', 3, 0, 8),
(2, '我们项目使用PTP协议做时间同步，精度可以达到微秒级别。', 3, 0, 15),
(3, 'CP是传统的ECU平台，主要用于实时控制；AP是面向高性能计算的平台，用于自动驾驶等场景。', 3, 1, 18);

-- 插入系统配置
INSERT INTO sys_config (config_key, config_value, description) VALUES
('knowledge_types', '["技术文档","设计方案","故障解决方案","仿真数据","测试报告","其他"]', '知识类型选项'),
('knowledge_domains', '["底盘工程","电子电控","软件算法","动力系统","整车设计","生产工艺","质量管控","其他"]', '知识领域选项'),
('upload_max_size', '104857600', '附件上传大小限制(字节)'),
('token_expire_hours', '24', 'JWT Token过期时间(小时)'),
('login_fail_limit', '5', '登录失败次数限制'),
('lock_duration_minutes', '30', '账户锁定时长(分钟)');
