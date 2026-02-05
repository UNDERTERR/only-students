package com.onlystudents.admin.service;

import com.onlystudents.admin.dto.request.AuditRequest;
import com.onlystudents.admin.entity.AuditRecord;

import java.util.List;

public interface AuditRecordService {

    void createAuditRecord(AuditRequest request, Long operatorId, String operatorName);

    List<AuditRecord> getAuditRecordsByTarget(Long targetId, Integer targetType);

    List<AuditRecord> getAuditRecordsByOperator(Long operatorId);

    List<AuditRecord> getPendingAuditRecords(Integer status);

    AuditRecord getAuditRecordById(Long id);
}
