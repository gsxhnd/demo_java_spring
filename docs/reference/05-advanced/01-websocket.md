# WebSocket / SSE 实时通信 / Real-time Communication

> 当 HTTP 请求-响应模式无法满足实时推送需求时，WebSocket 和 SSE 是两种主要的服务端推送方案。

## 1. 概述 / Overview

传统 HTTP 是客户端发起请求、服务端响应的单向模式。实时通信场景（聊天、通知、实时数据看板、协同编辑）需要服务端主动推送数据。Spring 提供了 WebSocket（全双工）和 SSE（服务端单向推送）两种方案。

## 2. 核心概念 / Core Concepts

### 2.1 实时通信方案对比

| 方案 | 协议 | 方向 | 连接 | 适用场景 |
|------|------|------|------|---------|
| 短轮询 (Polling) | HTTP | 客户端→服务端 | 反复建立 | 简单但低效 |
| 长轮询 (Long Polling) | HTTP | 客户端→服务端 | 保持直到有数据 | 兼容性好 |
| SSE | HTTP | 服务端→客户端 | 单向持久连接 | 通知、实时数据流 |
| WebSocket | WS/WSS | 双向 | 全双工持久连接 | 聊天、协同、游戏 |

### 2.2 WebSocket 协议

```
客户端                                    服务端
  │                                        │
  │──── HTTP Upgrade 请求 ────────────────▶│
  │     Connection: Upgrade                │
  │     Upgrade: websocket                 │
  │                                        │
  │◀─── 101 Switching Protocols ──────────│
  │                                        │
  │◀═══════ WebSocket 全双工通信 ═══════▶│
  │     双向发送文本/二进制帧               │
  │                                        │
  │──── Close Frame ──────────────────────▶│
  │◀─── Close Frame ──────────────────────│
```

### 2.3 STOMP 协议

STOMP（Simple Text Oriented Messaging Protocol）是 WebSocket 之上的子协议，提供消息语义（发布/订阅、点对点）。

```
┌─────────────────────────────────────────────────┐
│                  STOMP 消息模型                    │
│                                                  │
│  客户端                    服务端                  │
│    │                        │                    │
│    │── CONNECT ───────────▶│                    │
│    │◀─ CONNECTED ─────────│                    │
│    │                        │                    │
│    │── SUBSCRIBE /topic/chat ▶│  ← 订阅主题      │
│    │                        │                    │
│    │── SEND /app/chat ────▶│  ← 发送消息        │
│    │                        │── @MessageMapping  │
│    │                        │── 处理后转发        │
│    │◀─ MESSAGE /topic/chat ─│  ← 广播给订阅者    │
│    │                        │                    │
│    │── DISCONNECT ────────▶│                    │
└─────────────────────────────────────────────────┘
```

**STOMP 目的地前缀：**

| 前缀 | 说明 |
|------|------|
| `/app` | 应用目的地，路由到 `@MessageMapping` 方法 |
| `/topic` | 广播目的地，所有订阅者都收到 |
| `/queue` | 点对点目的地，只有特定用户收到 |
| `/user` | 用户专属目的地，`convertAndSendToUser()` |

### 2.4 SSE (Server-Sent Events)

```
客户端                                    服务端
  │                                        │
  │──── GET /stream ─────────────────────▶│
  │     Accept: text/event-stream          │
  │                                        │
  │◀─── 200 OK ──────────────────────────│
  │     Content-Type: text/event-stream    │
  │                                        │
  │◀─── data: {"price": 100}\n\n ────────│  ← 持续推送
  │◀─── data: {"price": 101}\n\n ────────│
  │◀─── data: {"price": 99}\n\n ─────────│
  │     ...                                │
```

**SSE vs WebSocket：**

| 特性 | SSE | WebSocket |
|------|-----|-----------|
| 方向 | 服务端→客户端（单向） | 双向 |
| 协议 | HTTP | WS/WSS |
| 自动重连 | 浏览器内置 | 需自行实现 |
| 数据格式 | 文本（UTF-8） | 文本 + 二进制 |
| 浏览器支持 | 除 IE 外全支持 | 全支持 |
| 代理/防火墙 | HTTP 友好 | 可能被拦截 |
| 适用场景 | 通知、数据流、日志 | 聊天、协同、游戏 |

**Spring 中的 SSE 实现方式：**

| 方式 | 说明 |
|------|------|
| `SseEmitter` | Spring MVC，基于 Servlet 异步 |
| `Flux<ServerSentEvent>` | Spring WebFlux，响应式 |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 方案 | 依赖 |
|------|------|
| WebSocket + STOMP | `spring-boot-starter-websocket` |
| SSE (MVC) | `spring-boot-starter-web`（内置） |
| SSE (WebFlux) | `spring-boot-starter-webflux` |

### 3.2 关键配置项

| 配置项 | 说明 |
|--------|------|
| WebSocket 端点路径 | `registry.addEndpoint("/ws")` |
| SockJS 回退 | `.withSockJS()` 兼容不支持 WS 的浏览器 |
| 消息大小限制 | `configureWebSocketTransport` 中设置 |
| 心跳间隔 | `configureMessageBroker` 中设置 heartbeat |

## 4. 进阶要点 / Advanced Topics

- **外部消息代理** — 生产环境用 RabbitMQ/ActiveMQ 替代内存消息代理，支持集群广播
- **WebSocket + Security** — 握手阶段认证（Token）+ STOMP 消息级别授权
- **集群 WebSocket** — 多实例部署时通过 Redis Pub/Sub 或 MQ 同步消息
- **连接管理** — 心跳检测、断线重连、连接数限制
- **SseEmitter 超时** — 默认 30s，长连接需设置更大超时或无限 `new SseEmitter(0L)`
- **背压处理** — 客户端消费慢时的缓冲和丢弃策略
- **二进制消息** — WebSocket 支持二进制帧，适合传输文件或 Protobuf

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| WebSocket 连接被 Nginx 断开 | Nginx 默认 60s 超时 | 配置 `proxy_read_timeout` 和心跳 |
| CORS 阻止 WebSocket | 跨域配置缺失 | `setAllowedOrigins` 或 SockJS |
| SSE 连接频繁断开 | 代理/负载均衡器超时 | 调整超时 + 心跳事件 |
| 集群环境消息不同步 | 内存消息代理只在本实例 | 使用外部消息代理 |
| 内存泄漏 | SseEmitter 未正确关闭 | 注册 `onCompletion` / `onTimeout` 回调清理 |

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-websocket-demo/`](../../examples/spring-websocket-demo/)

**演示功能：**
- WebSocket + STOMP 聊天室
- 房间管理（加入、离开）
- SSE 实时推送
- 点对点消息
- 定时广播通知

**运行示例：**
```bash
cd examples/spring-websocket-demo
mvn spring-boot:run
# 访问 http://localhost:8080
```

## 7. 参考链接 / References

- [Spring Framework Reference — WebSocket](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Spring Framework Reference — STOMP](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)
- [MDN — Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Baeldung — Spring WebSocket](https://www.baeldung.com/websockets-spring)
