package com.onlystudents.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.note.entity.NoteCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {

    /**
     * 根据分类ID查询分类名称
     */
    @Select("SELECT name FROM note_category WHERE id = #{categoryId}")
    String selectNameById(Long categoryId);
}
