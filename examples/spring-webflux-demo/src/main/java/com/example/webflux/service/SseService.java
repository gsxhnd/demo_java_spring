package com.example.webflux.service;

import com.example.webflux.dto.CreateProductRequest;
import com.example.webflux.dto.SseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SSE 服务 (响应式)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final ProductService productService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Sinks.Many<SseEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * 获取 SSE 事件流
     */
    public Flux<SseEvent> getEventStream() {
        return sink.asFlux();
    }

    /**
     * 发送 SSE 事件
     */
    public void sendEvent(String event, String data) {
        SseEvent sseEvent = SseEvent.of(event, data);
        sink.tryEmitNext(sseEvent);
        log.debug("SSE 事件发送: event={}, data={}", event, data);
    }

    /**
     * 实时时间流
     */
    public Flux<String> getTimeStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> LocalDateTime.now().format(FORMATTER))
                .log("time-stream");
    }

    /**
     * 产品库存变化流
     */
    public Flux<SseEvent> getProductUpdatesStream() {
        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> productService.getAllProducts().take(1))
                .map(products -> SseEvent.of("product-update",
                        String.format("库存更新: %d 个产品", products.size())))
                .log("product-stream");
    }

    /**
     * 搜索结果流（模拟实时搜索）
     */
    public Flux<SseEvent> searchWithDelay(String keyword) {
        return productService.searchProducts(keyword)
                .delayElements(Duration.ofMillis(500))  // 模拟网络延迟
                .map(product -> SseEvent.of("search-result",
                        String.format("找到: %s - ¥%s",
                                product.getName(), product.getPrice())));
    }
}
