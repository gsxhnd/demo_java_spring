package com.example.transaction.service;

import com.example.transaction.model.OperationLog;
import com.example.transaction.repository.OperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 操作日志服务
 */
@Service
public class LogService {

    private final OperationLogRepository operationLogRepository;

    public LogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Transactional(readOnly = true)
    public List<OperationLog> findAll() {
        return operationLogRepository.findAll();
    }
}
