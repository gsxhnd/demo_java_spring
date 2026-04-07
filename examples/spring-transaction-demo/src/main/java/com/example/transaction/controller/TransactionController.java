package com.example.transaction.controller;

import com.example.transaction.dto.ApiResponse;
import com.example.transaction.dto.CreateOrderRequest;
import com.example.transaction.dto.TransferRequest;
import com.example.transaction.model.Account;
import com.example.transaction.model.Order;
import com.example.transaction.model.OperationLog;
import com.example.transaction.service.AccountService;
import com.example.transaction.service.LogService;
import com.example.transaction.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事务管理演示控制器
 */
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final AccountService accountService;
    private final OrderService orderService;
    private final LogService logService;

    public TransactionController(AccountService accountService,
                                 OrderService orderService,
                                 LogService logService) {
        this.accountService = accountService;
        this.orderService = orderService;
        this.logService = logService;
    }

    /**
     * 初始化测试数据
     */
    @PostMapping("/init")
    public ApiResponse<String> initData() {
        accountService.initTestData();
        orderService.initTable();
        return ApiResponse.success("测试数据初始化完成", null);
    }

    // ========== 账户操作 ==========

    /**
     * 获取所有账户
     */
    @GetMapping("/accounts")
    public ApiResponse<List<Account>> getAllAccounts() {
        return ApiResponse.success(accountService.findAll());
    }

    /**
     * 获取指定账户
     */
    @GetMapping("/accounts/{accountNo}")
    public ApiResponse<Account> getAccount(@PathVariable String accountNo) {
        Account account = accountService.findByAccountNo(accountNo);
        return ApiResponse.success(account);
    }

    /**
     * 创建账户
     */
    @PostMapping("/accounts")
    public ApiResponse<Account> createAccount(
            @RequestParam String accountNo,
            @RequestParam String holderName,
            @RequestParam BigDecimal initialBalance) {
        Account account = accountService.createAccount(accountNo, holderName, initialBalance);
        return ApiResponse.success("账户创建成功", account);
    }

    /**
     * 存款
     */
    @PostMapping("/accounts/deposit")
    public ApiResponse<Account> deposit(
            @RequestParam String accountNo,
            @RequestParam BigDecimal amount) {
        Account account = accountService.deposit(accountNo, amount);
        return ApiResponse.success("存款成功", account);
    }

    /**
     * 转账（带事务）
     *
     * 演示 @Transactional 声明式事务
     */
    @PostMapping("/transfer")
    public ApiResponse<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(
                request.getFromAccountNo(),
                request.getToAccountNo(),
                request.getAmount()
        );

        Map<String, String> result = new HashMap<>();
        result.put("fromAccount", request.getFromAccountNo());
        result.put("toAccount", request.getToAccountNo());
        result.put("amount", request.getAmount().toString());
        result.put("status", "SUCCESS");

        return ApiResponse.success("转账成功", result);
    }

    // ========== 订单操作 ==========

    /**
     * 获取所有订单
     */
    @GetMapping("/orders")
    public ApiResponse<List<Order>> getAllOrders() {
        return ApiResponse.success(orderService.findAll());
    }

    /**
     * 获取指定订单
     */
    @GetMapping("/orders/{id}")
    public ApiResponse<Order> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.findById(id));
    }

    /**
     * 创建订单并扣款（完整事务）
     */
    @PostMapping("/orders")
    public ApiResponse<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrderAndDeduct(
                request.getOrderNo(),
                request.getUserId(),
                request.getProductName(),
                request.getQuantity(),
                request.getAmount(),
                request.getFromAccountNo()
        );
        return ApiResponse.success("订单创建成功", order);
    }

    // ========== 日志查询 ==========

    /**
     * 获取操作日志
     */
    @GetMapping("/logs")
    public ApiResponse<List<OperationLog>> getLogs() {
        return ApiResponse.success(logService.findAll());
    }

    // ========== 事务场景演示 ==========

    /**
     * 演示：转账失败（余额不足）
     * 预期：整个事务回滚，账户余额不变
     */
    @PostMapping("/demo/transfer-fail")
    public ApiResponse<Map<String, String>> demoTransferFail() {
        try {
            // 转出账户余额不足
            accountService.transfer("ACC001", "ACC002", new BigDecimal("999999999"));
            return ApiResponse.success("转账成功", null);
        } catch (Exception e) {
            return ApiResponse.error("转账失败（预期）: " + e.getMessage());
        }
    }

    /**
     * 演示：转账失败（金额超限触发业务异常）
     * 预期：整个事务回滚，账户余额不变
     */
    @PostMapping("/demo/transfer-business-fail")
    public ApiResponse<Map<String, String>> demoTransferBusinessFail() {
        try {
            // 金额超过 10000，触发业务验证失败
            accountService.transfer("ACC001", "ACC002", new BigDecimal("15000"));
            return ApiResponse.success("转账成功", null);
        } catch (Exception e) {
            return ApiResponse.error("转账失败（预期）: " + e.getMessage());
        }
    }

    /**
     * 演示：大额转账成功
     * 预期：转账成功，余额正确变更
     */
    @PostMapping("/demo/transfer-large")
    public ApiResponse<Map<String, String>> demoTransferLarge(
            @RequestParam(defaultValue = "1000") BigDecimal amount) {
        try {
            accountService.transfer("ACC001", "ACC002", amount);
            Map<String, String> result = new HashMap<>();
            result.put("message", "大额转账成功");
            result.put("amount", amount.toString());
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("转账失败: " + e.getMessage());
        }
    }
}
