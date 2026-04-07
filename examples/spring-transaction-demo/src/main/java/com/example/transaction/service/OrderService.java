package com.example.transaction.service;

import com.example.transaction.model.Order;
import com.example.transaction.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务 - 演示事务的多种场景
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final AccountService accountService;

    public OrderService(OrderRepository orderRepository, AccountService accountService) {
        this.orderRepository = orderRepository;
        this.accountService = accountService;
    }

    /**
     * 初始化表
     */
    public void initTable() {
        orderRepository.initTable();
    }

    /**
     * 创建订单并扣款（完整事务）
     *
     * 这个方法展示了一个完整的事务场景：
     * 1. 创建订单
     * 2. 扣减账户余额
     * 3. 如果任何一步失败，整个事务回滚
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderAndDeduct(String orderNo, Long userId, String productName,
                                     Integer quantity, BigDecimal amount, String fromAccountNo) {
        log.info("[OrderService] 创建订单: orderNo={}, amount={}", orderNo, amount);

        // 1. 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .productName(productName)
                .quantity(quantity)
                .amount(amount)
                .status("PENDING")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);
        log.info("[OrderService] 订单已创建: id={}", order.getId());

        // 2. 扣减余额（如果失败，订单也会回滚）
        accountService.deposit(fromAccountNo, amount.negate());
        log.info("[OrderService] 余额已扣减");

        // 3. 更新订单状态
        order.setStatus("PAID");
        order = orderRepository.save(order);
        log.info("[OrderService] 订单状态已更新为 PAID");

        // 4. 记录日志（REQUIRES_NEW，新事务）
        try {
            accountService.logOperation("CREATE_ORDER", "Order", order.getId(),
                    "创建订单: " + orderNo + ", 金额: " + amount);
        } catch (Exception e) {
            // 日志记录失败不应影响主事务
            log.warn("[OrderService] 日志记录失败: {}", e.getMessage());
        }

        return order;
    }

    /**
     * 批量创建订单（部分成功部分失败场景）
     *
     * 使用 NESTED 传播行为可以实现部分回滚
     */
    @Transactional(rollbackFor = Exception.class)
    public void createBatchOrders(List<Order> orders) {
        for (Order order : orders) {
            try {
                orderRepository.save(order);
                log.info("[OrderService] 订单已保存: {}", order.getOrderNo());
            } catch (Exception e) {
                log.error("[OrderService] 订单保存失败: {}", order.getOrderNo(), e);
                throw e; // 重新抛出，整个事务回滚
            }
        }
    }

    /**
     * 查询订单
     */
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * 查询所有订单
     */
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * 查询用户的订单
     */
    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
