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
    
    @Select("SELECT * FROM note WHERE status = 2 AND visibility = 0 ORDER BY hot_score DESC LIMIT #{limit}")
    List<Note> selectHotNotes(@Param("limit") Integer limit);
    
    @Select("SELECT * FROM note WHERE status = 2 AND visibility = 0 ORDER BY publish_time DESC LIMIT #{limit}")
    List<Note> selectLatestNotes(@Param("limit") Integer limit);
    
    @Update("UPDATE note SET view_count = view_count + 1 WHERE id = #{noteId}")
    int incrementViewCount(@Param("noteId") Long noteId);
    
    @Update("UPDATE note SET like_count = like_count + 1 WHERE id = #{noteId}")
    int incrementLikeCount(@Param("noteId") Long noteId);
    
    @Update("UPDATE note SET favorite_count = favorite_count + 1 WHERE id = #{noteId}")
    int incrementFavoriteCount(@Param("noteId") Long noteId);
    
    /**
     * 更新收藏数（设置为指定值）
     */
    @Update("UPDATE note SET favorite_count = #{count} WHERE id = #{noteId}")
    int updateFavoriteCount(@Param("noteId") Long noteId, @Param("count") Integer count);
    
    /**
     * 更新分享数
     */
    @Update("UPDATE note SET share_count = #{count} WHERE id = #{noteId}")
    int updateShareCount(@Param("noteId") Long noteId, @Param("count") Integer count);
    
    /**
     * 更新评分统计
     */
    @Update("UPDATE note SET rating = #{avgScore}, rating_count = #{count} WHERE id = #{noteId}")
    int updateRatingStats(@Param("noteId") Long noteId, @Param("avgScore") Double avgScore, @Param("count") Integer count);
    
    /**
     * 分页查询已发布的公开笔记（用于ES全量同步）
     * 只同步公开的笔记（visibility = 0），其他可见性级别的笔记不进入ES
     */
    @Select("SELECT * FROM note WHERE status = 2 AND visibility = 0 ORDER BY id LIMIT #{offset}, #{limit}")
    List<Note> selectPublishedNotesByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
}
