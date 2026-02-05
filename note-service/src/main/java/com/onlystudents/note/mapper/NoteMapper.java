package com.onlystudents.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.note.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    
    @Select("SELECT * FROM note WHERE status = 2 ORDER BY hot_score DESC LIMIT #{limit}")
    List<Note> selectHotNotes(@Param("limit") Integer limit);
    
    @Select("SELECT * FROM note WHERE status = 2 ORDER BY publish_time DESC LIMIT #{limit}")
    List<Note> selectLatestNotes(@Param("limit") Integer limit);
    
    @Update("UPDATE note SET view_count = view_count + 1 WHERE id = #{noteId}")
    int incrementViewCount(@Param("noteId") Long noteId);
    
    @Update("UPDATE note SET like_count = like_count + 1 WHERE id = #{noteId}")
    int incrementLikeCount(@Param("noteId") Long noteId);
    
    @Update("UPDATE note SET favorite_count = favorite_count + 1 WHERE id = #{noteId}")
    int incrementFavoriteCount(@Param("noteId") Long noteId);
}
