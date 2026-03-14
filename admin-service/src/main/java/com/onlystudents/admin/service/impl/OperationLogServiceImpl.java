package com.onlystudents.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.admin.entity.OperationLog;
import com.onlystudents.admin.mapper.OperationLogMapper;
import com.onlystudents.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {
    
    private final OperationLogMapper operationLogMapper;
    
    @Override
    public void saveLog(OperationLog logger) {
        if (logger.getCreatedAt() == null) {
            logger.setCreatedAt(LocalDateTime.now());
        }
        operationLogMapper.insert(logger);
        log.info("保存操作日志: adminId={}, operationType={}", logger.getAdminId(), logger.getOperationType());
    }
    
    @Override
    public Map<String, Object> getLogList(Integer page, Integer size, Long adminId, String operationType) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        if (adminId != null) {
            wrapper.eq(OperationLog::getAdminId, adminId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        
        Page<OperationLog> pageParam = new Page<>(page, size);
        Page<OperationLog> result = operationLogMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }
}
