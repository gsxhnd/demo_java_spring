# Distributed Transaction — 分布式事务 / Distributed Transaction

> Seata：AT / TCC / Saga 模式，解决跨服务数据一致性

## 1. 概述 / Overview

微服务架构下，一个业务操作可能跨多个服务和数据库。传统的本地事务无法保证跨服务的数据一致性，需要分布式事务方案。

### 典型场景

```
下单流程（跨 3 个服务）：

Order Service          Inventory Service       Account Service
  创建订单       →       扣减库存         →      扣减余额
     │                    │                      │
     └────────── 任何一步失败，全部回滚 ──────────┘
```

### 分布式事务方案对比

| 方案 | 一致性 | 性能 | 复杂度 | 适用场景 |
|---|---|---|---|---|
| **Seata AT** | 强一致（最终） | 中 | 低 | 通用业务（推荐入门） |
| **Seata TCC** | 强一致 | 高 | 高 | 高性能、资金类 |
| **Seata Saga** | 最终一致 | 高 | 中 | 长事务、流程编排 |
| **本地消息表** | 最终一致 | 高 | 中 | 异步场景 |
| **MQ 事务消息** | 最终一致 | 高 | 中 | RocketMQ 场景 |
| **2PC / XA** | 强一致 | 低 | 低 | 传统数据库 |

---

## 2. 核心概念 / Core Concepts

### Seata 架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ TM           │     │ RM           │     │ RM           │
│ (Transaction │     │ (Resource    │     │ (Resource    │
│  Manager)    │     │  Manager)    │     │  Manager)    │
│ Order Service│     │ Inventory Svc│     │ Account Svc  │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                     │
       │  1.Begin  2.Branch Register  3.Branch Register
       │                    │                     │
       ▼                    ▼                     ▼
┌─────────────────────────────────────────────────────┐
│                    TC (Transaction Coordinator)       │
│                    Seata Server                       │
│              协调全局事务提交/回滚                       │
└─────────────────────────────────────────────────────┘
```

| 角色 | 说明 |
|---|---|
| TC (Transaction Coordinator) | 事务协调器，Seata Server，维护全局事务状态 |
| TM (Transaction Manager) | 事务管理器，发起方，定义全局事务边界（`@GlobalTransactional`） |
| RM (Resource Manager) | 资源管理器，参与方，管理分支事务的资源 |

### AT 模式（Auto Transaction）

最常用，对业务代码零侵入：

```
Phase 1（执行）：
  1. 解析 SQL，记录 Before Image（修改前数据快照）
  2. 执行业务 SQL
  3. 记录 After Image（修改后数据快照）
  4. 生成 Undo Log，注册分支事务

Phase 2-Commit（提交）：
  异步删除 Undo Log（快速）

Phase 2-Rollback（回滚）：
  根据 Undo Log 的 Before Image 反向补偿
```

### TCC 模式（Try-Confirm-Cancel）

高性能但需要业务代码实现三个方法：

| 阶段 | 说明 | 示例（扣减库存） |
|---|---|---|
| Try | 资源预留 | 冻结库存（available - 10, frozen + 10） |
| Confirm | 确认提交 | 扣减冻结库存（frozen - 10） |
| Cancel | 取消回滚 | 释放冻结库存（available + 10, frozen - 10） |

### Saga 模式

长事务编排，每个步骤定义正向操作和补偿操作：

```
Step 1: 创建订单    ←→  补偿: 取消订单
Step 2: 扣减库存    ←→  补偿: 恢复库存
Step 3: 扣减余额    ←→  补偿: 退还余额

如果 Step 3 失败 → 执行 Step 2 补偿 → 执行 Step 1 补偿
```

---

## 3. 快速集成 / Quick Start

### Seata Server 部署

- Docker 部署：`seataio/seata-server`
- 存储模式：`file`（单机测试）/ `db`（MySQL 持久化，生产推荐）/ `redis`
- 注册中心：Nacos（推荐）/ Eureka / Consul

### Seata Client

- 依赖：`spring-cloud-starter-alibaba-seata`（包含 `seata-spring-boot-starter`）
- 关键配置：

| 配置 | 说明 |
|---|---|
| `seata.tx-service-group` | 事务分组名 |
| `seata.service.vgroup-mapping.<group>` | 分组到集群的映射 |
| `seata.registry.type` | 注册中心类型（nacos） |
| `seata.config.type` | 配置中心类型（nacos） |

### AT 模式额外要求

- 每个参与方数据库需创建 `undo_log` 表
- 数据源需被 Seata 代理（Spring Boot Starter 自动代理）

---

## 4. 进阶要点 / Advanced Topics

- **AT 模式全局锁**：写操作会获取全局锁，防止脏写，但会降低并发性能
- **TCC 空回滚 / 悬挂**：Try 未执行就收到 Cancel（空回滚），Cancel 后又收到 Try（悬挂），需要业务代码处理
- **TCC 幂等性**：Confirm 和 Cancel 必须幂等，可能被重复调用
- **Saga 状态机**：Seata 提供可视化状态机设计器，定义 Saga 流程
- **高可用部署**：Seata Server 集群 + Nacos 注册 + MySQL/Redis 存储
- **性能优化**：AT 模式异步提交、批量删除 Undo Log
- **与 ShardingSphere 集成**：分库分表场景下的分布式事务

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 全局事务超时 | 调大 `seata.client.tm.default-global-transaction-timeout` |
| AT 模式回滚失败 | 检查 Undo Log 中的 Before Image 是否被其他事务修改（脏写） |
| TCC 空回滚 | 在 Cancel 中检查 Try 是否执行过（查业务表或 TCC Fence 表） |
| Seata Server 连接失败 | 检查注册中心配置、事务分组映射 |
| 性能瓶颈 | AT 模式全局锁竞争，考虑切换 TCC 或 Saga |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [Seata 官方文档](https://seata.apache.org/docs/overview/what-is-seata)
- [Seata GitHub](https://github.com/apache/incubator-seata)
- [Spring Cloud Alibaba Seata](https://sca.aliyun.com/)
