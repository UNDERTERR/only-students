package com.onlystudents.note.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.note.entity.NoteTagRelation;
import lombok.Getter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteTagRelationMapper extends BaseMapper<NoteTagRelation> {
    
    /**
     * 根据笔记ID查询关联的标签ID列表
     */
    @Select("SELECT tag_id FROM note_tag_relation WHERE note_id = #{noteId}")
    List<Long> selectTagIdsByNoteId(@Param("noteId") Long noteId);
    
    /**
     * 根据笔记ID查询标签名称列表
     */
    @Select("SELECT t.name FROM note_tag t INNER JOIN note_tag_relation r ON t.id = r.tag_id WHERE r.note_id = #{noteId} AND t.status = 1")
    List<String> selectTagNamesByNoteId(@Param("noteId") Long noteId);

    /**
     * 批量查询多个笔记的标签（真正的批量SQL）
     */
    @Select("<script>" +
            "SELECT r.note_id, t.name FROM note_tag t " +
            "INNER JOIN note_tag_relation r ON t.id = r.tag_id " +
            "WHERE r.note_id IN " +
            "<foreach collection='noteIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND t.status = 1" +
            "</script>")
    List<NoteTagVO> selectTagNamesByNoteIds(@Param("noteIds") List<Long> noteIds);

    /**
     * NoteTagVO 用于批量查询结果
     */
    @Getter
    class NoteTagVO {
        private Long noteId;
        private String name;
    }
    
    /**
     * 根据笔记ID删除所有关联（使用 MyBatis Plus）
     */
    default int deleteByNoteId(Long noteId) {
        LambdaQueryWrapper<NoteTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTagRelation::getNoteId, noteId);
        return delete(wrapper);
    }
    
    /**
     * 根据标签ID查询关联的笔记ID列表
     */
    @Select("SELECT note_id FROM note_tag_relation WHERE tag_id = #{tagId}")
    List<Long> selectNoteIdsByTagId(@Param("tagId") Long tagId);
}
