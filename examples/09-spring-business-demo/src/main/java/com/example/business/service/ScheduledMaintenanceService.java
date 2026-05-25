package com.example.business.service;

import com.example.business.config.ScheduledProperties;
import com.example.business.dto.ScheduledTaskStatusResponse;
import com.example.business.entity.Order;
import com.example.business.entity.Order.OrderStatus;
import com.example.business.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledMaintenanceService {

    private final OrderRepository orderRepository;
    private final ScheduledProperties scheduledProperties;

    private final AtomicReference<LocalDateTime> lastRunAt = new AtomicReference<>();
    private final AtomicInteger lastCancelledCount = new AtomicInteger(0);

    @Scheduled(cron = "${app.scheduled.cancel-pending-orders-cron:0 */1 * * * *}")
    @Transactional
    public void cancelExpiredPendingOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<Order> expired = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, threshold);
        for (Order order : expired) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        if (!expired.isEmpty()) {
            orderRepository.saveAll(expired);
        }
        lastRunAt.set(LocalDateTime.now());
        lastCancelledCount.set(expired.size());
        log.info("[Scheduled] 取消超时 PENDING 订单 - count: {}", expired.size());
    }

    public ScheduledTaskStatusResponse getStatus() {
        return ScheduledTaskStatusResponse.builder()
                .lastRunAt(lastRunAt.get())
                .lastCancelledCount(lastCancelledCount.get())
                .cronExpression(scheduledProperties.getCancelPendingOrdersCron())
                .build();
    }
}
