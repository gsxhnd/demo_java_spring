# Elasticsearch — 搜索引擎集成 / Elasticsearch Integration

> Spring Boot + Elasticsearch：全文搜索、聚合分析、高亮显示

## 1. 概述 / Overview

Elasticsearch 是基于 Lucene 的分布式搜索和分析引擎。Spring Boot 通过 Spring Data Elasticsearch 集成。

### 适用场景

| 场景 | 说明 |
|---|---|
| 全文搜索 | 商品搜索、站内搜索 |
| 日志分析 | ELK Stack |
| 聚合统计 | 实时报表 |
| 自动补全 | 搜索建议 |
| 地理位置搜索 | 附近门店 |

### 概念对比

| Elasticsearch | 关系型数据库 |
|---|---|
| Index | Table |
| Document | Row |
| Field | Column |
| Mapping | Schema |
| Shard | Partition |

---

## 2. 核心概念 / Core Concepts

### 倒排索引 / Inverted Index

```
正排：Doc1 → "Spring Boot 入门教程"
倒排："Spring" → [Doc1, Doc2, Doc3]
      "Boot"   → [Doc1]
```

### 分析器 / Analyzer

```
原始文本 → Character Filter → Tokenizer(分词) → Token Filter(过滤) → 索引
```

| 分析器 | 适用 |
|---|---|
| `standard` | 英文（默认） |
| `ik_max_word` | 中文索引（最细粒度） |
| `ik_smart` | 中文搜索（智能分词） |

### 字段类型速查

| 类型 | 说明 |
|---|---|
| `text` | 全文搜索，会分词 |
| `keyword` | 精确匹配，不分词（聚合、排序） |
| `long` / `double` | 数值，范围查询 |
| `date` | 日期 |
| `nested` | 嵌套对象 |
| `geo_point` | 地理坐标 |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 配置速查

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: elastic123
    connection-timeout: 5s
    socket-timeout: 30s
```

### 核心注解

| 注解 | 作用 |
|---|---|
| `@Document(indexName = "xxx")` | 映射 Index |
| `@Id` | 文档 ID |
| `@Field(type = FieldType.Text, analyzer = "ik_max_word")` | 字段类型和分析器 |
| `@MultiField` | 多字段映射（text + keyword） |
| `@Setting(shards = 3, replicas = 1)` | 分片和副本 |

---

## 4. 进阶要点 / Advanced Topics

- **ElasticsearchOperations**：`NativeQuery` 构建复杂查询（bool、match、range、aggs）
- **高亮搜索**：`@Highlight` 注解或 `HighlightQuery`
- **聚合分析**：Terms Aggregation（分组统计）、Avg/Sum/Max（数值聚合）
- **自动补全**：`completion` 类型 + `SuggestBuilder`
- **同义词**：自定义同义词过滤器
- **索引别名 (Alias)**：零停机重建索引
- **Bulk 批量操作**：`BulkOperations` 批量索引/更新
- **数据同步**：MySQL → ES 同步方案（Canal / Logstash / 双写）

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 中文搜索不准 | 安装 IK 分词器插件 |
| 深分页性能差 | 用 `search_after` 替代 `from + size` |
| 数据与 MySQL 不一致 | 异步同步 + 补偿机制 |
| 索引 Mapping 变更 | 创建新索引 → Reindex → 切换别名 |
| 集群 Yellow/Red | 检查分片分配、磁盘空间、节点状态 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-es-demo/`](../../examples/spring-es-demo/)

包含：Document 实体、Repository 查询、ElasticsearchOperations 复杂搜索、聚合、高亮。

启动依赖：
```bash
cd devops && docker compose -f elasticsearch-compose.yml up -d
```

## 7. 参考链接 / References

- [Spring Data Elasticsearch 官方文档](https://docs.spring.io/spring-data/elasticsearch/reference/)
- [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/)
- [IK 分词器](https://github.com/infinilabs/analysis-ik)
