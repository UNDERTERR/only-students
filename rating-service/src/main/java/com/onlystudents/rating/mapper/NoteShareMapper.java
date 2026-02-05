package com.onlystudents.rating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.rating.entity.NoteShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoteShareMapper extends BaseMapper<NoteShare> {
    
    /**
     * 根据分享码查询
     */
    @Select("SELECT * FROM note_share WHERE share_code = #{shareCode} LIMIT 1")
    NoteShare selectByShareCode(@Param("shareCode") String shareCode);
    
    /**
     * 查询笔记分享数
     */
    @Select("SELECT COUNT(*) FROM note_share WHERE note_id = #{noteId}")
    Long countByNoteId(@Param("noteId") Long noteId);
    
    /**
     * 增加点击次数
     */
    @Update("UPDATE note_share SET click_count = click_count + 1 WHERE id = #{id}")
    void incrementClickCount(@Param("id") Long id);
    
    /**
     * 查询用户的分享列表
     */
    @Select("SELECT * FROM note_share WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<NoteShare> selectListByUser(@Param("userId") Long userId);
}
