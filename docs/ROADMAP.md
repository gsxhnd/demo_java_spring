# Roadmap

reference 文档（00-44）与 examples 示例项目（01-12）的对应关系及开发进度。

## 总体进度

| 阶段 | 示例项目 | reference | 状态 |
|------|----------|-----------|------|
| Part 1 准备阶段 | 01-spring-java-basics-demo | 00-04 | ✅ 已完成 |
| Part 2 核心概念 | 02-spring-core-demo | 05-09 | ✅ 已完成 |
| Part 3 Spring Boot 起步 | 03-spring-boot-demo / 04-spring-boot-observability-demo | 10-15 | ✅ 已完成 |
| Part 4 Web 开发 | 05-spring-web-demo | 16-21 | ✅ 已完成 |
| Part 5 数据访问（JPA） | 06-spring-data-jpa-demo | 22-26, 28 | ✅ 已完成 |
| Part 6 数据访问（MyBatis） | 07-spring-data-mybatis-demo | 27-28 | ✅ 已完成 |
| Part 7 数据访问（其他数据库） | 08-spring-multi-database-demo | 28 | ✅ 已完成 |
| Part 8 业务能力 | 09-spring-business-demo | 30-34 | ✅ 已完成 |
| Part 9 测试 | 10-spring-testing-demo | 35-37 | ✅ 已完成 |
| Part 10 安全 | 11-spring-security-demo | 29, 38-40 | ⬜ 待开发 |
| Part 11 通信协议 | 12-spring-communication-demo | 41-44 | ⬜ 待开发 |

---

## 已完成

### Part 1：准备阶段 → `examples/01-spring-java-basics-demo`

纯 Java SE，无 Spring 依赖，入口为 `public static void main()`。

| reference | 标题 | 核心演示 |
|-----------|------|----------|
| 00 | Java Spring 术语表 | — |
| 01 | Java Spring | — |
| 02 | Java 注解机制 | 自定义 `@Component` / `@Inject` / `@Process` + 运行时注解处理 |
| 03 | Java 反射基础 | `Class` / `Constructor` / `Field` / `Method` API，`setAccessible` |
| 04 | Java Lambda 与函数式接口 | `Function` / `Consumer` / `Supplier` / `Predicate`，Stream API，方法引用 |

运行：`mvn compile exec:java`

### Part 2：核心概念 → `examples/02-spring-core-demo`

Spring IoC 容器（`AnnotationConfigApplicationContext`），无 Web 服务器。

| reference | 标题 | 核心演示 |
|-----------|------|----------|
| 05 | Java Spring IoC | `@Configuration` + `@ComponentScan`，手动创建容器 |
| 06 | Java Spring Bean | `@Service` / `@Repository`，Bean 生命周期（`@PostConstruct` / `@PreDestroy`） |
| 07 | Java Spring DI | 构造器注入，面向接口编程（`UserService` → `UserServiceImpl`） |
| 08 | Java Spring AOP | `@Aspect` + 全部 5 种通知类型（Before/After/Around/AfterReturning/AfterThrowing） |
| 09 | Java Spring 容器 | `ApplicationContext` 自省（`getBeanDefinitionNames` / `getBean`） |

运行：`mvn exec:java`

### Part 3：Spring Boot 起步 → `examples/03-spring-boot-demo` & `examples/04-spring-boot-observability-demo`

| reference | 标题 | 归属 |
|-----------|------|------|
| 10 | Spring Boot 概述 | 03 |
| 11 | Spring Boot 自动配置 | 03 |
| 12 | Spring Boot Starter | 03 |
| 13 | Spring Boot 项目结构 | 03 |
| 14 | Spring Boot 配置 | 03 |
| 15 | Spring 可观测性 | 04 |

**03-spring-boot-demo**：`@SpringBootApplication`、`@ConfigurationProperties` vs `@Value`、REST 端点

**04-spring-boot-observability-demo**：Actuator、OpenTelemetry、Micrometer（Counter/Timer/Gauge）、`@Observed`、ECS 结构化日志

### Part 4：Web 开发 → `examples/05-spring-web-demo`

| reference | 标题 | 核心演示 |
|-----------|------|----------|
| 16 | Java Spring Controller | `@RestController` + `@RequestMapping`，完整 CRUD |
| 17 | Java Spring 请求处理 | `@PathVariable` / `@RequestBody` / `@Valid` |
| 18 | Java Spring 响应与 DTO | `CreateUserRequest` / `UserResponse` / `ResponseEntity` |
| 19 | Java Spring 异常处理 | `@ControllerAdvice` + `@ExceptionHandler`（404/400/500） |
| 20 | Java Spring 参数校验 | Jakarta Bean Validation（`@NotBlank` / `@Email` / `@Min` / `@Max`） |
| 21 | Java Spring OpenAPI | SpringDoc + Swagger UI |

### Part 5：数据访问（JPA）→ `examples/06-spring-data-jpa-demo`

Spring Data JPA + MySQL，读写分离双数据源，Web 三层架构完整对接。

| reference | 标题 | 核心演示 |
|-----------|------|----------|
| 22 | ORM 与 JPA | `@Entity` / `@Table` / `@Id` / `@GeneratedValue` / `@Column`，`ddl-auto: update` 自动建表 |
| 23 | Spring Data JPA | `JpaRepository` 接口命名查询、`@Query` 自定义 JPQL / 原生 SQL、`Pageable` 分页排序 |
| 24 | Entity | JPA 审计（`@CreatedDate` / `@LastModifiedDate`）、`@Enumerated`、`@Embeddable` / `@Embedded` |
| 25 | Repository | `CrudRepository` / `JpaRepository` 层级区别，自定义查询方法 |
| 26 | 事务管理 | `@Transactional` 声明式事务（类级 `readOnly=true`），`propagation = REQUIRES_NEW`，`isolation = READ_COMMITTED` |
| 28 | 多数据库 | 双 `DataSource` + `EntityManagerFactory` + `TransactionManager` Bean 配置，`@Primary` / `@Qualifier` 数据源路由 |

运行：`mvn spring-boot:run`

### Part 6：数据访问（MyBatis）→ `examples/07-spring-data-mybatis-demo`

MyBatis 独立项目，注解 SQL + XML Mapper 动态 SQL，读写分离双数据源。

| reference | 标题 | 核心演示 |
|-----------|------|----------|
| 27 | MyBatis | `@Mapper` / `@Select` / `@Insert` / `@Update` / `@Delete` 注解方式，XML Mapper 文件，`@Param` 参数绑定 |
| 27 | 动态 SQL | `<if>` / `<where>` / `<foreach>` / `<sql>` / `<include>` 标签，`${}` 动态排序 |
| 28 | 多数据库 | 双 `DataSource` + `SqlSessionFactory` + `SqlSessionTemplate` Bean 配置，`@MapperScan` 分包，`@Primary` / `@Qualifier` |

运行：`mvn spring-boot:run`

### Part 7：数据访问（其他数据库）→ `examples/08-spring-multi-database-demo`

单项目集成 Redis / MongoDB / Elasticsearch / InfluxDB，按 API 前缀分区，对照 [28 多数据库](./reference/28_Java%20Spring%20多数据库.md)。

中间件：`docker compose -f devops/full-stack-compose.yml up -d redis mongodb elasticsearch influxdb`

| reference | 模块 | API 前缀 | 核心演示 |
|-----------|------|----------|----------|
| 28 | Redis | `/api/redis/**` | `RedisTemplate` 缓存读写与 TTL，`StringRedisTemplate` + `setIfAbsent` 分布式锁 |
| 28 | MongoDB | `/api/mongo/logs/**` | `@Document` + `MongoRepository`，`Map` 灵活字段，`Pageable` 分页 |
| 28 | Elasticsearch | `/api/es/products/**` | `@Document` 索引 + `ElasticsearchRepository`，关键词检索 |
| 28 | InfluxDB | `/api/influx/metrics/**` | `influxdb-client-java`，`Point` 写入 + Flux 查询 |

运行：`mvn spring-boot:run`（需上述中间件已启动）

### Part 8：业务能力 → `examples/09-spring-business-demo`

单项目演示电商下单场景，依赖 MySQL + Redis。

中间件：`docker compose -f devops/full-stack-compose.yml up -d mysql redis`

| reference | 标题 | 核心演示 |
|-----------|------|----------|
| 30 | Service 层 | `OrderService` 封装下单业务（库存校验、扣减、事务边界），Controller 仅做协议转换 |
| 31 | 配置管理 | `AppProperties` / `CacheProperties` 等 `@ConfigurationProperties`，`application-dev.yml` / `application-prod.yml` Profile |
| 32 | 异步与定时任务 | `NotificationService` + `@Async`，`ScheduledMaintenanceService` + `@Scheduled` 取消超时订单 |
| 33 | 缓存 | `ProductService` 上 `@Cacheable` / `@CachePut` / `@CacheEvict`，Redis `RedisCacheManager` |
| 34 | 文件处理 | `MultipartFile` 上传、`Resource` 下载、静态资源映射 `/files/**` |

| API | 说明 |
|-----|------|
| `/api/products/**` | 商品 CRUD（含缓存演示） |
| `/api/orders` | 下单（触发异步通知） |
| `/api/files/**` | 文件上传/下载 |
| `/api/app/info` | 当前 Profile 与配置快照 |
| `/api/app/scheduled-status` | 定时任务执行状态 |

运行：`mvn spring-boot:run`

---

### Part 9：测试 → `examples/10-spring-testing-demo`

用户 CRUD 示例应用 + 完整测试套件（H2 内存库，无需外部中间件）。

| reference | 测试类 | 关键内容 |
|-----------|--------|----------|
| 35 | `UserServiceTest` | JUnit 5 + `@ExtendWith(MockitoExtension.class)`，`@Mock` / `@InjectMocks`，`ArgumentCaptor` |
| 36 | `UserMockMvcIntegrationTest` | `@SpringBootTest` + `@AutoConfigureMockMvc`，完整 HTTP 链路 |
| 36 | `UserRestTemplateIntegrationTest` | `@SpringBootTest(RANDOM_PORT)` + `@AutoConfigureTestRestTemplate` |
| 37 | `UserControllerWebMvcTest` | `@WebMvcTest` + `@MockitoBean` + `MockMvc` |
| 37 | `UserRepositoryDataJpaTest` | `@DataJpaTest` + `TestEntityManager` |
| 37 | `UserResponseJsonTest` | `@JsonTest` + `JacksonTester`（snake_case JSON） |

运行：`mvn test`（18 个测试，按 `@Tag` 分为 unit / slice / integration）

Spring Boot 4 测试依赖：`spring-boot-starter-webmvc-test`、`spring-boot-starter-data-jpa-test`、`spring-boot-starter-jackson-test`、`spring-boot-starter-restclient`（test scope）

---

## 待开发

### Part 10：安全 → `examples/11-spring-security-demo`

| reference | 标题 | 关键内容 |
|-----------|------|----------|
| 38 | Spring Security | 安全架构，过滤器链，SecurityFilterChain |
| 39 | 认证与授权 | 表单登录，`UserDetailsService`，角色权限 |
| 40 | JWT | 无状态认证，Token 签发与验证，刷新令牌 |
| 29 | Casbin | RBAC/ABAC 访问控制模型 |

### Part 11：通信协议 → `examples/12-spring-communication-demo`

| reference | 标题 | 关键内容 |
|-----------|------|----------|
| 41 | WebSocket | STOMP，`@MessageMapping`，SockJS 回退 |
| 42 | MQTT | Spring Integration MQTT，消息发布/订阅 |
| 43 | Modbus | Modbus TCP 主站/从站，工业协议 |
| 44 | 通信协议选型 | 协议对比与选型决策 |

---

## 版本基线

| 组件 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Framework | 7.0.6 |
| Maven | 3.9+ |

## 项目命名规范

```
examples/{两位数序号}-spring-{topic}-demo/
```

- 包结构：`com.example.{topic}` → `controller/` `service/` `repository/` `entity/` `dto/` `config/` `exception/`
- 入口类：`{Topic}DemoApplication.java`，注解 `@SpringBootApplication`
- 配置：`application.yml`（不使用 `.properties`）
- 注入：构造器注入 + `private final` 字段
- Lombok：`optional:true`，排除出 `spring-boot-maven-plugin`
