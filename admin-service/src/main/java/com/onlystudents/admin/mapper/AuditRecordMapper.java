package com.onlystudents.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.admin.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {

    @Select("SELECT * FROM audit_record WHERE target_id = #{targetId} AND target_type = #{targetType} ORDER BY created_at DESC")
    List<AuditRecord> selectByTarget(@Param("targetId") Long targetId, @Param("targetType") Integer targetType);

    @Select("SELECT * FROM audit_record WHERE operator_id = #{operatorId} ORDER BY created_at DESC")
    List<AuditRecord> selectByOperator(@Param("operatorId") Long operatorId);

    @Select("SELECT * FROM audit_record WHERE status = #{status} ORDER BY created_at DESC")
    List<AuditRecord> selectByStatus(@Param("status") Integer status);
}
