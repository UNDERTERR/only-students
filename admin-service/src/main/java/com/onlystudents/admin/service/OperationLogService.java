package com.onlystudents.admin.service;

import com.onlystudents.admin.entity.OperationLog;
import java.util.List;
import java.util.Map;

public interface OperationLogService {
    
    void saveLog(OperationLog log);
    
    Map<String, Object> getLogList(Integer page, Integer size, Long adminId, String operationType);
}
