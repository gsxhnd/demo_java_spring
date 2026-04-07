-- Docker Demo 数据库初始化脚本

-- 创建应用表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入示例数据
INSERT INTO products (name, description, price, stock) VALUES
('Spring Boot 实战', 'Spring Boot 权威指南', 89.00, 100),
('Java 核心技术', 'Java 核心技术卷 I', 119.00, 50),
('Docker 容器实战', 'Docker 从入门到实践', 69.00, 80),
('Kubernetes 权威指南', 'Kubernetes 入门与实践', 99.00, 60),
('微服务设计', '微服务架构设计指南', 79.00, 40);
