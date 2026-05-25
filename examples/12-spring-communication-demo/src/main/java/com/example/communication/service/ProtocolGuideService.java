package com.example.communication.service;

import com.example.communication.dto.ProtocolMatrixRow;
import com.example.communication.dto.ProtocolRecommendRequest;
import com.example.communication.dto.ProtocolRecommendRequest.Scenario;
import com.example.communication.dto.ProtocolRecommendResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProtocolGuideService {

    public List<ProtocolMatrixRow> matrix() {
        return List.of(
                new ProtocolMatrixRow(
                        "HTTP/REST", "请求-响应", "客户端主动", "TCP (HTTP/1.1, HTTP/2)",
                        "JSON / XML", "原生", "通用 Web API、移动端 CRUD"),
                new ProtocolMatrixRow(
                        "gRPC", "请求-响应 + 流", "客户端主动 + 服务端流", "TCP (HTTP/2)",
                        "Protobuf", "需 grpc-web", "微服务内部高性能调用"),
                new ProtocolMatrixRow(
                        "WebSocket + STOMP", "全双工", "双向", "TCP (HTTP 升级)",
                        "文本/JSON", "原生", "浏览器聊天、通知、协作"),
                new ProtocolMatrixRow(
                        "MQTT", "发布-订阅", "经 Broker 任意方向", "TCP",
                        "二进制（极小）", "需 MQTT over WS", "IoT 传感器、低功耗设备"),
                new ProtocolMatrixRow(
                        "Modbus TCP/RTU", "主-从轮询", "Master 主动", "TCP / RS-485",
                        "二进制寄存器", "不支持", "PLC、变频器、工业仪表"));
    }

    public ProtocolRecommendResponse recommend(ProtocolRecommendRequest request) {
        Scenario scenario = request.getScenario();
        return switch (scenario) {
            case WEB_API -> ProtocolRecommendResponse.builder()
                    .scenario(scenario.name())
                    .recommendedProtocol("HTTP/REST")
                    .rationale("浏览器与移动端生态成熟，OpenAPI 文档与网关支持完善，无明确性能瓶颈时默认首选。")
                    .alternatives(List.of("gRPC（需 grpc-web 转码）"))
                    .demoEndpoint("/swagger-ui.html")
                    .build();
            case MICROSERVICE_INTERNAL -> ProtocolRecommendResponse.builder()
                    .scenario(scenario.name())
                    .recommendedProtocol("gRPC")
                    .rationale("HTTP/2 多路复用 + Protobuf 编码，延迟与带宽优于 REST + JSON，契约由 .proto 强类型生成。")
                    .alternatives(List.of("HTTP/REST（团队熟悉度更高时）"))
                    .demoEndpoint(null)
                    .build();
            case BROWSER_REALTIME -> ProtocolRecommendResponse.builder()
                    .scenario(scenario.name())
                    .recommendedProtocol("WebSocket + STOMP")
                    .rationale("浏览器原生支持全双工；STOMP 提供 /topic 广播与 @MessageMapping，开发体验接近 Controller。")
                    .alternatives(List.of("SSE（仅服务端推送）"))
                    .demoEndpoint("/chat.html")
                    .build();
            case IOT_SENSORS -> ProtocolRecommendResponse.builder()
                    .scenario(scenario.name())
                    .recommendedProtocol("MQTT")
                    .rationale("极低报文开销、QoS 分级、遗嘱消息，经 Broker 解耦海量设备与后端服务。")
                    .alternatives(List.of("HTTP（设备数量少、网络稳定时）"))
                    .demoEndpoint("/api/mqtt/status")
                    .build();
            case INDUSTRIAL_PLC -> ProtocolRecommendResponse.builder()
                    .scenario(scenario.name())
                    .recommendedProtocol("Modbus TCP/RTU")
                    .rationale("工业设备事实标准，统一寄存器模型跨品牌读写；本示例内置 TCP 从站模拟器。")
                    .alternatives(List.of("MQTT（经边缘网关转译后上云）"))
                    .demoEndpoint("/api/modbus/registers")
                    .build();
            case LARGE_FILE_TRANSFER -> ProtocolRecommendResponse.builder()
                    .scenario(scenario.name())
                    .recommendedProtocol("HTTP Multipart / gRPC 流")
                    .rationale("小文件用 HTTP Multipart；持续大流用 gRPC streaming。WebSocket 对一次性大文件无优势。")
                    .alternatives(List.of("WebSocket 二进制帧（长连接场景）"))
                    .demoEndpoint(null)
                    .build();
        };
    }
}
