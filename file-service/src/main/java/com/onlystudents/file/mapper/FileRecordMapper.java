package com.onlystudents.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.file.entity.FileRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {

    @Select("SELECT * FROM file_record WHERE md5_hash = #{md5Hash} AND status = 1 LIMIT 1")
    FileRecord selectByMd5Hash(@Param("md5Hash") String md5Hash);
    @Select("SELECT * FROM file_record WHERE file_name = #{fileName} AND status = 1 LIMIT 1")
    FileRecord selectByFileName(@Param("fileName") String fileName);
    @Delete("DELETE FROM file_record WHERE id = #{fileId}")
    int physicalDeleteById(@Param("fileId") Long fileId);
}
