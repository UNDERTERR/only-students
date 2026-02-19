package com.onlystudents.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.note.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
    
    /**
     * 根据名称查询标签
     */
    @Select("SELECT * FROM note_tag WHERE name = #{name} AND status = 1 LIMIT 1")
    NoteTag selectByName(@Param("name") String name);
    
    /**
     * 批量查询标签
     */
    List<NoteTag> selectByNames(@Param("names") List<String> names);
    
    /**
     * 增加标签使用次数
     */
    int incrementUsageCount(@Param("tagId") Long tagId);
    
    /**
     * 减少标签使用次数
     */
    int decrementUsageCount(@Param("tagId") Long tagId);
    
    /**
     * 获取热门标签
     */
    @Select("SELECT * FROM note_tag WHERE status = 1 ORDER BY usage_count DESC LIMIT #{limit}")
    List<NoteTag> selectHotTags(@Param("limit") Integer limit);
}
