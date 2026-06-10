-- ============================================================
-- 示例数据 (密码明文均为 123456, BCrypt加密后存储)
-- ============================================================
USE student_knowledge_db;

-- 用户 (密码: 123456)
INSERT INTO t_user (id, username, email, password, role) VALUES
(1, 'admin',    'admin@school.edu',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6KXIO', 'ADMIN'),
(2, '20210001', 'zhang@school.edu',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6KXIO', 'STUDENT'),
(3, '20210002', 'li@school.edu',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6KXIO', 'STUDENT'),
(4, '20210003', 'wang@school.edu',     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6KXIO', 'STUDENT');

-- 分类（用户2的文件树）
INSERT INTO t_category (id, name, parent_id, user_id, sort_order) VALUES
(1, '计算机科学', NULL, 2, 1),
(2, 'Java编程',   1,    2, 1),
(3, '数据结构',   1,    2, 2),
(4, '高等数学',   NULL, 2, 2),
(5, '英语学习',   NULL, 2, 3),
(6, 'Spring框架', 2,    2, 1);

-- 标签
INSERT INTO t_tag (id, name, color, user_id) VALUES
(1, '重点',     '#f5222d', 2),
(2, '复习',     '#faad14', 2),
(3, '期末',     '#722ed1', 2),
(4, '实验',     '#13c2c2', 2),
(5, '课堂笔记', '#52c41a', 2);

-- 知识条目
INSERT INTO t_knowledge_entry (id, title, content, user_id, category_id) VALUES
(1, 'Java多态详解',        '## 多态\n多态是面向对象三大特性之一...\n\n### 方法重写\n子类可以重写父类的方法...', 2, 2),
(2, '链表反转算法',        '## 链表反转\n```java\npublic ListNode reverse(ListNode head) {\n    ListNode prev = null;\n    ...\n}\n```', 2, 3),
(3, '极限与连续',          '## 极限定义\nε-δ 语言定义极限...', 2, 4),
(4, 'SpringBoot自动配置原理', '## @SpringBootApplication\n该注解包含三个核心注解...', 2, 6),
(5, '英语四级高频词汇',     '## Word List 1\n- abandon v. 放弃\n- abstract adj. 抽象的\n...', 2, 5);

-- 知识-标签关联
INSERT INTO t_knowledge_tag (entry_id, tag_id) VALUES
(1, 1), (1, 5),
(2, 1), (2, 4),
(3, 2), (3, 3),
(4, 5),
(5, 2), (5, 3);

-- 日程任务
INSERT INTO t_task (id, title, description, user_id, status, priority, start_time, deadline) VALUES
(1, '完成数据结构实验报告', '链表反转、二叉树遍历实验', 2, 'IN_PROGRESS', 'HIGH',   '2026-06-01 08:00:00', '2026-06-10 23:59:59'),
(2, '复习高等数学第三章',   '极限、连续、导数',         2, 'TODO',        'MEDIUM', NULL,                   '2026-06-15 23:59:59'),
(3, '英语四级模拟考试',     '完成两套模拟试卷',         2, 'TODO',        'URGENT', NULL,                   '2026-06-08 09:00:00'),
(4, 'SpringBoot项目答辩准备','准备PPT和演示',           2, 'IN_PROGRESS', 'HIGH',   '2026-06-02 08:00:00', '2026-06-20 17:00:00'),
(5, '整理Java笔记',        '汇总本学期所有Java笔记',   2, 'DONE',        'LOW',    '2026-05-20 08:00:00', '2026-05-30 23:59:59');
