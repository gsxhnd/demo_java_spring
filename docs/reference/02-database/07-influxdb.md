# InfluxDB — 时序数据库集成 / InfluxDB Integration

> Spring Boot + InfluxDB：监控指标、IoT 传感器、时间线数据

## 1. 概述 / Overview

InfluxDB 是最流行的开源时序数据库（TSDB），专为时间戳数据设计。Spring Boot 中通过 InfluxDB 官方 Java Client 集成（无 Spring Data 官方支持）。

### 适用场景

| 场景 | 说明 |
|---|---|
| 应用监控 | CPU、内存、QPS 等指标采集 |
| IoT 传感器 | 温度、湿度、GPS 等时序数据 |
| 业务指标 | 订单量、GMV 等业务时间线 |
| DevOps | 服务器监控、容器监控 |
| 金融行情 | 股票/加密货币价格走势 |

### InfluxDB 数据模型

```
Measurement: cpu_usage          ← 类似表名
Tags:        host=server01, region=us-east  ← 索引字段（字符串，用于过滤和分组）
Fields:      usage_idle=98.5, usage_user=1.2  ← 值字段（数值，用于计算）
Timestamp:   2024-01-15T08:30:00Z           ← 时间戳（必须）
```

| 概念 | 类比 SQL | 说明 |
|---|---|---|
| Measurement | Table | 数据表 |
| Tag | 索引列 | 自动索引，用于 WHERE / GROUP BY |
| Field | 普通列 | 不索引，存储实际值 |
| Timestamp | 主键 | 每条数据必须有时间戳 |
| Bucket | Database | 数据桶（InfluxDB 2.x） |
| Organization | - | 组织（InfluxDB 2.x 多租户） |

### InfluxDB 2.x vs 1.x

| 特性 | InfluxDB 2.x | InfluxDB 1.x |
|---|---|---|
| 查询语言 | **Flux**（函数式） | InfluxQL（类 SQL） |
| 认证 | Token 认证 | 用户名密码 |
| 存储概念 | Bucket | Database + Retention Policy |
| UI | 内置 Web UI | 需要 Chronograf |

本文档基于 **InfluxDB 2.x**。

---

## 2. 核心概念 / Core Concepts

### Flux 查询语言

Flux 是 InfluxDB 2.x 的函数式查询语言，通过管道 `|>` 串联操作：

```flux
from(bucket: "monitoring")
  |> range(start: -1h)                          // 时间范围
  |> filter(fn: (r) => r._measurement == "cpu") // 过滤
  |> filter(fn: (r) => r.host == "server01")
  |> aggregateWindow(every: 5m, fn: mean)       // 5分钟平均值
  |> yield(name: "mean_cpu")
```

### 常用 Flux 函数

| 函数 | 说明 |
|---|---|
| `from()` | 指定数据源 Bucket |
| `range()` | 时间范围过滤 |
| `filter()` | 条件过滤 |
| `aggregateWindow()` | 时间窗口聚合（mean/sum/max/min/count） |
| `group()` | 分组 |
| `sort()` | 排序 |
| `limit()` | 限制返回行数 |
| `pivot()` | 行转列 |
| `map()` | 数据转换 |

### 数据保留策略 / Retention Policy

InfluxDB 支持自动过期删除历史数据：

| 策略 | 说明 |
|---|---|
| 7 天 | 实时监控数据 |
| 30 天 | 短期分析 |
| 365 天 | 长期趋势 |
| 无限 | 永久保留 |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

```xml
<!-- InfluxDB 2.x Java Client -->
<dependency>
    <groupId>com.influxdb</groupId>
    <artifactId>influxdb-client-java</artifactId>
    <version>7.2.0</version>
</dependency>
```

### 配置

```yaml
# 自定义配置（非 Spring 标准）
influxdb:
  url: http://localhost:8086
  token: my-super-secret-token
  org: my-org
  bucket: monitoring
```

### 配置类

```java
@Configuration
public class InfluxDBConfig {

    @Value("${influxdb.url}")
    private String url;

    @Value("${influxdb.token}")
    private String token;

    @Value("${influxdb.org}")
    private String org;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }
}
```

---

## 4. 进阶要点 / Advanced Topics

- **写入方式**：`WriteApiBlocking`（同步）、`WriteApi`（异步批量，推荐生产使用）
- **POJO 映射**：`@Measurement` / `@Column` 注解，直接映射 Java 对象
- **批量写入配置**：`batchSize`、`flushInterval`、`retryInterval` 控制写入策略
- **降采样 (Downsampling)**：用 InfluxDB Task 定时聚合，减少存储量
- **连续查询 (Task)**：类似定时任务，自动执行 Flux 查询并写入结果
- **与 Grafana 集成**：InfluxDB 作为 Grafana 数据源，构建监控面板
- **与 Telegraf 集成**：Telegraf 采集系统指标，写入 InfluxDB
- **健康检查**：`influxDBClient.health()` 检查连接状态

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| Token 认证失败 | 在 InfluxDB UI 中生成 Token，确认权限范围 |
| 写入性能差 | 使用异步 `WriteApi` + 批量写入 |
| Flux 查询慢 | 确保 Tag 字段用于过滤，Field 不建索引 |
| Tag 基数过高 | Tag 值不要用 UUID 等高基数值，会导致索引膨胀 |
| 数据量过大 | 配置 Retention Policy 自动清理 + 降采样 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-influxdb-demo/`](../../examples/spring-influxdb-demo/)

包含：InfluxDBConfig、POJO 写入、Flux 查询、异步批量写入、健康检查。

启动依赖：
```bash
cd devops && docker compose -f influxdb-compose.yml up -d
```

## 7. 参考链接 / References

- [InfluxDB 2.x 官方文档](https://docs.influxdata.com/influxdb/v2/)
- [InfluxDB Java Client](https://github.com/influxdata/influxdb-client-java)
- [Flux 查询语言](https://docs.influxdata.com/flux/)
