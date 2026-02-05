package com.onlystudents.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.payment.entity.CompensationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CompensationTaskMapper extends BaseMapper<CompensationTask> {
    
    /**
     * 查询待处理的补偿任务
     */
    @Select("SELECT * FROM compensation_task WHERE status = 0 AND next_execute_time <= NOW() " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<CompensationTask> selectPendingTasks(@Param("limit") Integer limit);
    
    /**
     * 查询处理中的超时任务（超过5分钟）
     */
    @Select("SELECT * FROM compensation_task WHERE status = 1 AND updated_at < DATE_SUB(NOW(), INTERVAL 5 MINUTE) " +
            "ORDER BY updated_at ASC LIMIT #{limit}")
    List<CompensationTask> selectTimeoutTasks(@Param("limit") Integer limit);
    
    /**
     * 根据业务ID和类型查询任务
     */
    @Select("SELECT * FROM compensation_task WHERE business_id = #{businessId} AND task_type = #{taskType} LIMIT 1")
    CompensationTask selectByBusinessIdAndType(@Param("businessId") Long businessId, @Param("taskType") String taskType);
    
    /**
     * 增加重试次数
     */
    @Update("UPDATE compensation_task SET retry_count = retry_count + 1, updated_at = NOW() WHERE id = #{id}")
    int incrementRetryCount(@Param("id") Long id);
    
    /**
     * 标记任务为处理中
     */
    @Update("UPDATE compensation_task SET status = 1, updated_at = NOW() WHERE id = #{id} AND status = 0")
    int markProcessing(@Param("id") Long id);
    
    /**
     * 标记任务为成功
     */
    @Update("UPDATE compensation_task SET status = 2, success_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int markSuccess(@Param("id") Long id);
    
    /**
     * 标记任务为失败，设置下次执行时间
     */
    @Update("UPDATE compensation_task SET status = 0, next_execute_time = #{nextTime}, " +
            "error_msg = #{errorMsg}, updated_at = NOW() WHERE id = #{id}")
    int markFailed(@Param("id") Long id, @Param("nextTime") LocalDateTime nextTime, @Param("errorMsg") String errorMsg);
    
    /**
     * 标记任务为放弃（超过最大重试次数）
     */
    @Update("UPDATE compensation_task SET status = 4, error_msg = #{errorMsg}, updated_at = NOW() WHERE id = #{id}")
    int markAbandoned(@Param("id") Long id, @Param("errorMsg") String errorMsg);
    
    /**
     * 统计失败任务数（用于监控告警）
     */
    @Select("SELECT COUNT(*) FROM compensation_task WHERE status = 3 OR (status = 0 AND retry_count >= 3)")
    Long countFailedTasks();
}
