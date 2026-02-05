package com.onlystudents.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.report.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    
    @Select("SELECT * FROM report WHERE reporter_id = #{reporterId} AND deleted = 0 ORDER BY created_at DESC")
    List<Report> selectByReporterId(Long reporterId);
    
    @Select("SELECT * FROM report WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Report> selectByStatus(Integer status);
    
    @Select("SELECT * FROM report WHERE target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0")
    List<Report> selectByTarget(Long targetId, Integer targetType);
}
