-- R__seed_data.sql
-- 可重复执行的种子数据迁移

MERGE INTO categories (id, name, description, parent_id, created_at, last_modified_at)
KEY(name)
VALUES (1, 'Technology', '技术相关文章', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO categories (id, name, description, parent_id, created_at, last_modified_at)
KEY(name)
VALUES (2, 'Java', 'Java 技术', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO categories (id, name, description, parent_id, created_at, last_modified_at)
KEY(name)
VALUES (3, 'Spring', 'Spring 框架', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO categories (id, name, description, parent_id, created_at, last_modified_at)
KEY(name)
VALUES (4, 'Life', '生活随笔', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
