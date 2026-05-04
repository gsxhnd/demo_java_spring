-- 初始化数据
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO products (name, description, price, stock, category) VALUES
('MacBook Pro 14', 'Apple M3 Pro 芯片笔记本', 14999.00, 50, 'Electronics'),
('iPhone 15 Pro', 'A17 Pro 芯片手机', 8999.00, 200, 'Electronics'),
('AirPods Pro 2', '主动降噪无线耳机', 1899.00, 500, 'Electronics'),
('Spring Boot 实战', 'Spring Boot 权威指南', 89.00, 100, 'Books'),
('Clean Code', '代码整洁之道', 79.00, 80, 'Books'),
('ThinkPad X1', '商务轻薄笔记本', 12999.00, 30, 'Electronics'),
('iPad Air', 'M1 芯片平板', 4999.00, 150, 'Electronics'),
('Docker 实战', 'Docker 容器入门', 69.00, 200, 'Books');
