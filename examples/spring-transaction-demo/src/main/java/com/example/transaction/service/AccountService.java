package com.example.transaction.service;

import com.example.transaction.model.Account;
import com.example.transaction.model.OperationLog;
import com.example.transaction.repository.AccountRepository;
import com.example.transaction.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户服务 - 演示声明式事务
 *
 * @Transactional 声明式事务的生效条件：
 * 1. 方法必须是 public
 * 2. 调用必须通过代理对象（不能自调用）
 * 3. 异常必须抛出且未被 catch 吞掉
 * 4. 数据库引擎必须支持事务（如 InnoDB）
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final OperationLogRepository operationLogRepository;

    public AccountService(AccountRepository accountRepository,
                          OperationLogRepository operationLogRepository) {
        this.accountRepository = accountRepository;
        this.operationLogRepository = operationLogRepository;
    }

    /**
     * 创建账户（带事务，只读优化）
     */
    @Transactional(readOnly = true)
    public Account createAccount(String accountNo, String holderName, BigDecimal initialBalance) {
        Account account = Account.builder()
                .accountNo(accountNo)
                .holderName(holderName)
                .balance(initialBalance)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        return accountRepository.save(account);
    }

    /**
     * 转账操作（带事务）
     *
     * REQUIRED 传播行为：有事务就加入，没有就新建
     */
    @Transactional(rollbackFor = Exception.class)
    public void transfer(String fromAccountNo, String toAccountNo, BigDecimal amount) {
        log.info("[AccountService.transfer] 开始转账: from={}, to={}, amount={}",
                fromAccountNo, toAccountNo, amount);

        // 获取转出账户
        Account fromAccount = accountRepository.findByAccountNo(fromAccountNo);
        if (fromAccount == null) {
            throw new IllegalArgumentException("转出账户不存在: " + fromAccountNo);
        }

        // 获取转入账户
        Account toAccount = accountRepository.findByAccountNo(toAccountNo);
        if (toAccount == null) {
            throw new IllegalArgumentException("转入账户不存在: " + toAccountNo);
        }

        // 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("余额不足: 余额=" + fromAccount.getBalance() + ", 转账=" + amount);
        }

        // 执行转账
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        BigDecimal newToBalance = toAccount.getBalance().add(amount);

        accountRepository.updateBalance(fromAccountNo, newFromBalance);
        log.info("[AccountService.transfer] 转出账户 {} 余额变为: {}", fromAccountNo, newFromBalance);

        // 模拟业务处理（可能抛出异常）
        processTransfer(fromAccountNo, toAccountNo, amount);

        accountRepository.updateBalance(toAccountNo, newToBalance);
        log.info("[AccountService.transfer] 转入账户 {} 余额变为: {}", toAccountNo, newToBalance);

        log.info("[AccountService.transfer] 转账完成");
    }

    /**
     * 处理转账（模拟可能失败的业务逻辑）
     */
    private void processTransfer(String from, String to, BigDecimal amount) {
        // 模拟业务处理
        // 如果金额大于 10000，模拟业务失败
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            log.warn("[AccountService.processTransfer] 业务验证失败: 大额转账需要人工审批");
            throw new RuntimeException("大额转账需要人工审批");
        }
    }

    /**
     * 存款操作（带事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public Account deposit(String accountNo, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("存款金额必须大于 0");
        }

        Account account = accountRepository.findByAccountNo(accountNo);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在: " + accountNo);
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        accountRepository.updateBalance(accountNo, newBalance);

        return accountRepository.findByAccountNo(accountNo);
    }

    /**
     * 查询账户（只读事务）
     */
    @Transactional(readOnly = true)
    public Account findByAccountNo(String accountNo) {
        return accountRepository.findByAccountNo(accountNo);
    }

    /**
     * 查询所有账户
     */
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    /**
     * 记录操作日志
     *
     * REQUIRES_NEW 传播行为：总是新建独立事务
     * 即使外部事务回滚，日志也会被保存
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logOperation(String operation, String entityType, Long entityId, String details) {
        OperationLog logEntry = OperationLog.builder()
                .operation(operation)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .createTime(LocalDateTime.now())
                .build();
        operationLogRepository.save(logEntry);
        log.info("[AccountService.logOperation] 记录日志: operation={}, entity={}:{}",
                operation, entityType, entityId);
    }

    /**
     * 初始化测试数据
     */
    public void initTestData() {
        accountRepository.initTable();
        operationLogRepository.initTable();

        // 创建测试账户
        createAccount("ACC001", "张三", new BigDecimal("10000.00"));
        createAccount("ACC002", "李四", new BigDecimal("5000.00"));
        createAccount("ACC003", "王五", new BigDecimal("8000.00"));
    }
}
