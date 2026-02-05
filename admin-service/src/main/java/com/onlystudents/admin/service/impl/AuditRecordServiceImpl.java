package com.onlystudents.admin.service.impl;

import com.onlystudents.admin.dto.request.AuditRequest;
import com.onlystudents.admin.entity.AuditRecord;
import com.onlystudents.admin.mapper.AuditRecordMapper;
import com.onlystudents.admin.service.AuditRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditRecordServiceImpl implements AuditRecordService {

    private final AuditRecordMapper auditRecordMapper;

    @Override
    @Transactional
    public void createAuditRecord(AuditRequest request, Long operatorId, String operatorName) {
        AuditRecord record = new AuditRecord();
        BeanUtils.copyProperties(request, record);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setStatus(1);
        record.setAuditTime(LocalDateTime.now());

        auditRecordMapper.insert(record);
    }

    @Override
    public List<AuditRecord> getAuditRecordsByTarget(Long targetId, Integer targetType) {
        return auditRecordMapper.selectByTarget(targetId, targetType);
    }

    @Override
    public List<AuditRecord> getAuditRecordsByOperator(Long operatorId) {
        return auditRecordMapper.selectByOperator(operatorId);
    }

    @Override
    public List<AuditRecord> getPendingAuditRecords(Integer status) {
        return auditRecordMapper.selectByStatus(status);
    }

    @Override
    public AuditRecord getAuditRecordById(Long id) {
        return auditRecordMapper.selectById(id);
    }
}
