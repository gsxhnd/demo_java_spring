# Message Queue — 消息队列 / Message Queue

> RabbitMQ / Kafka：异步通信、削峰填谷、事件驱动架构

## 1. 概述 / Overview

消息队列是微服务间异步通信的核心组件，用于解耦服务、削峰填谷、保证最终一致性。

### 同步 vs 异步通信

```
同步（OpenFeign）：              异步（MQ）：

A ──→ B ──→ C                  A ──→ MQ ──→ B
     等待...等待...                    ↘──→ C
     ←── 响应 ←──                A 不等待，继续处理
```

### 消息队列适用场景

| 场景 | 说明 |
|---|---|
| 异步处理 | 注册后发邮件/短信，不阻塞主流程 |
| 削峰填谷 | 秒杀请求先入队，后端按能力消费 |
| 服务解耦 | 订单服务不直接调用库存/积分/通知服务 |
| 事件驱动 | 发布领域事件，多个消费者各自处理 |
| 数据同步 | MySQL → MQ → Elasticsearch / Redis |
| 延迟任务 | 延迟队列：30 分钟未支付自动取消订单 |

---

## 2. 核心概念 / Core Concepts

### RabbitMQ vs Kafka

| 特性 | RabbitMQ | Kafka |
|---|---|---|
| 模型 | 消息队列（Queue） | 分布式日志（Topic + Partition） |
| 协议 | AMQP | 自定义协议 |
| 消息顺序 | 单队列有序 | 单 Partition 有序 |
| 吞吐量 | 万级 QPS | 百万级 QPS |
| 消息持久化 | 可选 | 默认持久化（磁盘顺序写） |
| 消费模式 | Push（推） | Pull（拉） |
| 消息回溯 | 不支持（消费即删） | 支持（按 offset 回溯） |
| 延迟队列 | 原生支持（插件） | 不原生支持 |
| 适用场景 | 业务消息、任务队列 | 日志采集、大数据流、事件流 |
| 运维复杂度 | 中 | 高（依赖 ZooKeeper/KRaft） |

### RabbitMQ 核心模型

```
Producer → Exchange → Binding → Queue → Consumer

Exchange 类型：
  Direct   ── 精确匹配 Routing Key
  Topic    ── 通配符匹配（*.order.#）
  Fanout   ── 广播到所有绑定队列
  Headers  ── 按 Header 匹配（少用）
```

### Kafka 核心模型

```
Producer → Topic → Partition 0 ──→ Consumer Group A
                 → Partition 1 ──→   (Consumer 1)
                 → Partition 2 ──→   (Consumer 2)
                                ──→ Consumer Group B
                                     (独立消费)
```

| 概念 | 说明 |
|---|---|
| Topic | 消息主题（逻辑分类） |
| Partition | 分区（并行度单位，单分区有序） |
| Offset | 消费位移（消费者记录消费到哪里） |
| Consumer Group | 消费者组（组内竞争消费，组间广播） |
| Broker | Kafka 服务节点 |

### Spring Cloud Stream

Spring Cloud Stream 提供了消息中间件的统一抽象，屏蔽 RabbitMQ / Kafka 的差异：

```
Application
     │
     ▼
Spring Cloud Stream (Binder 抽象)
     │
  ┌──┴──┐
  ▼     ▼
RabbitMQ  Kafka
Binder    Binder
```

---

## 3. 快速集成 / Quick Start

### RabbitMQ

- 依赖：`spring-boot-starter-amqp`
- 关键配置：

| 配置 | 说明 |
|---|---|
| `spring.rabbitmq.host` | 地址 |
| `spring.rabbitmq.port` | 端口（5672） |
| `spring.rabbitmq.username` / `password` | 认证 |
| `spring.rabbitmq.virtual-host` | 虚拟主机 |
| `spring.rabbitmq.listener.simple.acknowledge-mode` | 确认模式（manual 推荐） |
| `spring.rabbitmq.listener.simple.prefetch` | 预取数量 |

### Kafka

- 依赖：`spring-kafka`
- 关键配置：

| 配置 | 说明 |
|---|---|
| `spring.kafka.bootstrap-servers` | Broker 地址 |
| `spring.kafka.consumer.group-id` | 消费者组 |
| `spring.kafka.consumer.auto-offset-reset` | 无 offset 时策略（earliest/latest） |
| `spring.kafka.consumer.enable-auto-commit` | 自动提交（建议 false，手动提交） |
| `spring.kafka.producer.acks` | 确认级别（all 最安全） |
| `spring.kafka.producer.retries` | 重试次数 |

### Spring Cloud Stream

- 依赖：`spring-cloud-starter-stream-rabbit` 或 `spring-cloud-starter-stream-kafka`
- 编程模型：函数式（`Function<Flux<Input>, Flux<Output>>`）

---

## 4. 进阶要点 / Advanced Topics

- **消息可靠性**：生产者确认（Publisher Confirm）+ 消息持久化 + 消费者手动 ACK
- **消息幂等性**：消费者需处理重复消息（唯一 ID + 去重表 / Redis 去重）
- **死信队列 (DLQ)**：消费失败的消息转入死信队列，人工处理或重试
- **延迟队列**：RabbitMQ 延迟插件 / Kafka 时间轮 / Redis ZSet
- **消息顺序性**：RabbitMQ 单队列有序；Kafka 同一 Key 路由到同一 Partition
- **消息积压处理**：增加消费者实例、临时扩容队列/分区
- **事务消息**：RocketMQ 半消息机制（RabbitMQ/Kafka 不原生支持）
- **Kafka Exactly-Once**：`enable.idempotence=true` + 事务 Producer

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 消息丢失 | 开启 Publisher Confirm + 持久化 + 手动 ACK |
| 消息重复消费 | 消费者幂等处理（去重表 / Redis SETNX） |
| 消费者阻塞 | 检查消费逻辑是否有慢查询/死锁 |
| Kafka Rebalance 频繁 | 调大 `max.poll.interval.ms`，减小 `max.poll.records` |
| RabbitMQ 连接断开 | 配置自动重连 `spring.rabbitmq.connection-timeout` |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [Spring AMQP (RabbitMQ) 官方文档](https://docs.spring.io/spring-amqp/reference/)
- [Spring for Apache Kafka 官方文档](https://docs.spring.io/spring-kafka/reference/)
- [Spring Cloud Stream 官方文档](https://docs.spring.io/spring-cloud-stream/reference/)
- [RabbitMQ 官方文档](https://www.rabbitmq.com/docs)
- [Apache Kafka 官方文档](https://kafka.apache.org/documentation/)
