# 示例项目 / Example Projects

> 核心基础篇示例项目，每个项目独立可运行（Spring Boot 4.0.5 + Java 21）

## 项目列表

| 项目 | 技术栈 | 说明 |
|------|--------|------|
| [spring-ioc-demo](spring-ioc-demo/) | ApplicationContext, @Component, @Autowired | IoC 容器、Bean 生命周期、作用域、条件装配 |
| [spring-mvc-demo](spring-mvc-demo/) | @RestController, @Valid, @ControllerAdvice | RESTful API、参数校验、统一异常处理、拦截器 |
| [spring-autoconfig-demo](spring-autoconfig-demo/) | @SpringBootApplication, @ConfigurationProperties, Profile | 自动配置原理、配置绑定、多环境切换 |
| [spring-transaction-demo](spring-transaction-demo/) | @Transactional, Propagation, Isolation | 声明式事务、传播行为、事务失效场景 |

## 运行示例项目

```bash
# 从 examples 目录进入任一项目
cd spring-ioc-demo
mvn spring-boot:run
```

## 通用说明

- 版本：Spring Boot 4.0.5 + Java 21
- 使用 Maven 构建，无 mvnw wrapper
- 默认端口 8080（可在 application.yml 中修改）
- 未使用外部中间件（spring-transaction-demo 使用 H2 内存数据库）
