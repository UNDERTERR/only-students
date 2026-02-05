package com.onlystudents.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.file.entity.FileConvertTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileConvertTaskMapper extends BaseMapper<FileConvertTask> {
    
    @Select("SELECT * FROM file_convert_task WHERE task_status = 0 ORDER BY created_at ASC LIMIT 10")
    List<FileConvertTask> selectPendingTasks();
    
    @Select("SELECT * FROM file_convert_task WHERE source_file_id = #{sourceFileId} ORDER BY created_at DESC LIMIT 1")
    FileConvertTask selectLatestTaskBySourceFileId(Long sourceFileId);
}
