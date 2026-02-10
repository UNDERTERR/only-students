-- 插入测试笔记数据
-- 执行: mysql -u root -p only_students_note < insert_test_notes.sql

USE only_students_note;

-- 插入测试笔记（确保status=2已发布）
INSERT INTO note (
    title, 
    content, 
    user_id, 
    status, 
    visibility, 
    education_level, 
    subject, 
    hot_score, 
    view_count, 
    like_count, 
    favorite_count, 
    comment_count, 
    publish_time, 
    created_at, 
    updated_at
) VALUES 
(
    'xiaojie的学习笔记', 
    '这是xiaojie分享的学习笔记内容，包含了很多有用的知识点和学习方法。', 
    101,  -- 假设用户ID 101 存在
    2,  -- status=2 已发布
    0,  -- visibility=0 公开
    4,  -- education_level=4 大学
    '计算机科学', 
    100.0, 
    50, 
    10, 
    5, 
    3, 
    NOW(), 
    NOW(), 
    NOW()
);

-- 查询确认
SELECT id, title, status, username FROM note WHERE title LIKE '%xiaojie%';