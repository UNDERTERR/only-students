package com.onlystudents.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.file.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
    
    @Select("SELECT * FROM file_record WHERE md5_hash = #{md5Hash} AND status = 1 LIMIT 1")
    FileRecord selectByMd5Hash(String md5Hash);
    
    @Select("SELECT * FROM file_record WHERE file_name = #{fileName} AND status = 1 LIMIT 1")
    FileRecord selectByFileName(String fileName);
}
