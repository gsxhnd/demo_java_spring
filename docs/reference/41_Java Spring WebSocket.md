---
title: Java Spring WebSocket
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - WebSocket
  - STOMP
  - 实时通信
---

<!-- markdownlint-disable MD025 -->

# Java Spring WebSocket

## 为什么要学 WebSocket

前面八个 Part 中，所有的通信都是基于 HTTP 的请求-响应模式 — 客户端发请求，服务端返回响应，一次通信就结束了。这种模式对于查询数据、提交表单等场景非常合适，但对于需要**服务端主动推送**的场景就很别扭了：聊天消息推送、股票价格实时更新、协同编辑的实时同步、任务进度的主动通知...

在这些场景下，HTTP 只能通过轮询（Polling）来模拟"实时" — 每几秒钟发一个请求问"有新消息吗？"。这种方式浪费带宽（绝大多数请求返回空数据），延迟高（取决于轮询间隔），服务端压力大。WebSocket 是解决"服务端需要主动向客户端推送数据"这个问题的标准协议。

## 核心概念

### WebSocket 是什么

WebSocket 是一种在单个 TCP 连接上进行全双工通信的协议。它在客户端和服务端之间建立一个持久连接，双方都可以在这个连接上随时发送数据，不需要等待对方的请求。

**换个说法：** HTTP 像对讲机 — 你按一下说一句，对方回一句，说完频道就关了。WebSocket 像打电话 — 拨通后双方可以同时说话，不需要等对方说完才能开口，也不用频繁"喂喂喂"确认在线。

### 为什么需要 WebSocket

**痛点场景：** 一个在线客服系统，用户发送消息后，客服需要在 2 秒内看到并回复。用 HTTP 轮询实现：前端每 2 秒发一个 `GET /messages?since=xxx` 请求。1000 个在线用户 = 每秒 500 个请求打到后端。其中 98% 的请求返回空数据（没有新消息）。服务端大量的连接和查询浪费在处理空轮询上。

**设计动机：** WebSocket 建立后，服务端只在有新消息时才推送数据。没有消息时连接静默，占用极少资源。从"客户端不断敲门问"变成"服务端有东西再叫你"。

### 没有 WebSocket 会怎样

**困境：** 用 HTTP 长轮询（Long Polling）或 Server-Sent Events（SSE）作为替代方案。长轮询相比短轮询减少了请求数，但服务端仍需管理大量悬挂的 HTTP 连接。SSE 只能服务端到客户端单向推送，客户端发消息仍需另开 HTTP 请求。两者都不如 WebSocket 的全双工、单连接模式优雅。

**有了 WebSocket 之后：** 一个连接搞定双向通信。服务端可以广播消息给所有连接、给特定用户发消息、给房间内用户群发。这些模式天然适合聊天、通知、协作等场景。

## 概念深入解释

### WebSocket 握手过程

WebSocket 连接始于一个 HTTP 升级请求：

```
客户端发起 HTTP 请求：
GET /chat HTTP/1.1
Host: example.com
Upgrade: websocket
Connection: Upgrade

服务端响应 101 Switching Protocols：
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
```

握手完成后，协议从 HTTP 升级为 WebSocket，同一个 TCP 连接进入全双工模式。

### Spring WebSocket 支持

Spring 提供了两套 WebSocket 方案：

**1. 原始 WebSocket（低层级）**

直接处理 WebSocket 消息，适合自定义协议：

```java
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 连接建立
    }

    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) {
        // 收到消息
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }
}
```

**2. STOMP over WebSocket（推荐）**

STOMP（Simple Text Oriented Messaging Protocol）是 WebSocket 之上的子协议，提供消息代理、发布/订阅、目的地路由等高级抽象：

```
客户端 ──→ /app/chat.send  ──→ @MessageMapping 处理 ──→ /topic/chat  ──→ 所有订阅者
```

```java
@Controller
public class ChatController {

    @MessageMapping("/chat.send")          // 客户端发送到 /app/chat.send
    @SendTo("/topic/public")                // 处理结果广播到 /topic/public
    public ChatMessage sendMessage(ChatMessage message) {
        return message;
    }
}
```

### Spring STOMP 配置

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");  // 内置消息代理
        registry.setApplicationDestinationPrefixes("/app"); // 应用路由前缀
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")          // WebSocket 连接端点
            .setAllowedOriginPatterns("*")
            .withSockJS();                    // 兜底方案，不支持 WS 时降级
    }
}
```

### 消息路由规则

```
客户端发送到：/app/chat.send
        │
        ▼
    @MessageMapping("/chat.send")  ← 匹配 /app 前缀后的路径
        │
        ▼
    @SendTo("/topic/public")       ← 返回值广播到消息代理
        │
        ▼
所有订阅了 /topic/public 的客户端收到消息
```

**三种路由模式：**

| 目的地前缀 | 含义 | 示例 |
|-----------|------|------|
| `/app/*` | 应用处理路由（到 @MessageMapping） | `/app/chat.send` |
| `/topic/*` | 广播消息（一对多） | `/topic/public` |
| `/queue/*` | 点对点消息（一对一） | `/queue/notifications` |

### 安全与认证

WebSocket 连接也需要认证。Spring Security 对 WebSocket 的支持：

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/ws/**").permitAll()        // 允许连接
    .requestMatchers("/app/**").authenticated()   // 发消息需认证
    .requestMatchers("/topic/**").authenticated() // 订阅需认证
);
```

对于 STOMP，可以在 CONNECT 帧中携带认证头。Spring 通过 `ChannelInterceptor` 拦截 STOMP 消息进行处理。

### 消息代理对比

| 类型 | 适用场景 | 说明 |
|------|----------|------|
| 内置 Simple Broker | 开发环境、单实例部署 | 内存级，不支持集群 |
| 外部 Broker（RabbitMQ） | 生产环境、多实例部署 | 支持集群、持久化、更丰富的路由 |

使用外部 Broker 的配置：

```java
registry.enableStompBrokerRelay("/topic", "/queue")
    .setRelayHost("rabbitmq-host")
    .setRelayPort(61613);
```

## 核心要点

1. **WebSocket 是全双工协议：** 一个连接双向通信，解决服务端主动推送的需求。
2. **STOMP 比原始 WebSocket 更好用：** STOMP 提供发布/订阅、目的地路由，开发体验更接近写 Controller。
3. **内置 Broker 只适合单实例：** 生产环境多实例部署时，需用 RabbitMQ 等外部 Broker 做消息中继。
4. **WebSocket 连接从 HTTP 升级而来：** 初始握手是 HTTP，101 状态码后协议切换。
5. **别忘了配置 CORS：** WebSocket 握手是 HTTP 请求，跨域需要处理。SockJS 兜底方案也需要 HTTP CORS 配置。

## 常见误区

- **WebSocket 连接数过多压垮线程池。** Spring 默认用 `SimpleAsyncTaskExecutor`（或 Tomcat 的 WebSocket 线程模型），高并发下需要合理配置线程资源和连接限制。实际承载能力取决于操作系统文件描述符限制和内存。
- **在 STOMP @MessageMapping 方法中做耗时操作。** 消息处理方法会阻塞 STOMP 的消息分发线程。耗时操作应该异步化（`@Async` 或提交到线程池），让消息方法快速返回。
- **认为 WebSocket 会自动重连。** WebSocket 连接断开后不会自动重连。前端需要实现重连逻辑（如 SockJS 自带断线重连，STOMP.js 也有相应实现）。
- **WebSocket 和 HTTP 的 SecurityContext 不共享。** WebSocket 连接建立时的握手是 HTTP，可以进行认证。但连接建立后，`SecurityContextHolder`（ThreadLocal）的行为和 HTTP 请求不同。STOMP 层面的安全应通过 `ChannelInterceptor` 独立处理。
- **忘记配置消息大小限制。** WebSocket 消息默认可能有大小限制。Spring 的内置 Broker 默认限制为 64KB（`org.springframework.messaging`）。传输大消息时需调整。
- **混淆 `/app` 和 `/topic` 前缀的作用。** `/app/*` 是客户端发送消息给服务端的路由（到 Controller），`/topic/*` 是服务端广播给客户端的路由（从 Broker 到订阅者）。误把 `/app` 当成广播目的地会导致消息到达了 Controller 但订阅者收不到。

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- STOMP Controller 的编程模型（@MessageMapping）与 REST Controller（@GetMapping）相似
- **前置：** [Java Spring Security](./38_Java%20Spring%20Security.md) -- WebSocket 连接也需要认证和授权
- **前置：** [Java Spring 异步与定时任务](./32_Java%20Spring%20异步与定时任务.md) -- WebSocket 消息处理中耗时操作用 @Async 避免阻塞
- **并行：** [Java Spring MQTT](./42_Java%20Spring%20MQTT.md) -- MQTT 是 IoT 领域的轻量级发布/订阅协议，与 WebSocket 有不同的适用场景
- **并行：** [Java Spring 通信协议选型](./44_Java%20Spring%20通信协议选型.md) -- 不同场景下通信协议的选型决策
- **后续：** [Java Spring Cloud 消息队列](../Spring_Cloud/Java Spring Cloud 消息队列.md) -- 多实例下 WebSocket 消息需要通过消息队列在实例间广播
