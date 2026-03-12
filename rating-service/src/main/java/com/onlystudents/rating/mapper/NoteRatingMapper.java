package com.onlystudents.rating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.rating.entity.NoteRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteRatingMapper extends BaseMapper<NoteRating> {
    
    /**
     * 查询用户的评分
     */
    @Select("SELECT * FROM note_rating WHERE note_id = #{noteId} AND user_id = #{userId} LIMIT 1")
    NoteRating selectByNoteAndUser(@Param("noteId") Long noteId, @Param("userId") Long userId);
    
    /**
     * 查询笔记平均评分
     */
    @Select("SELECT AVG(score) FROM note_rating WHERE note_id = #{noteId}")
    Double selectAverageScoreByNoteId(@Param("noteId") Long noteId);
    
    /**
     * 查询笔记评分人数
     */
    @Select("SELECT COUNT(*) FROM note_rating WHERE note_id = #{noteId}")
    Long countByNoteId(@Param("noteId") Long noteId);
    
    /**
     * 查询用户的评分列表
     */
    @Select("SELECT * FROM note_rating WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<NoteRating> selectListByUser(@Param("userId") Long userId);
    
    /**
     * 查询创作者所有笔记的评分统计
     * 需要通过 note_service 查询创作者的笔记，再统计评分
     */
    @Select("SELECT AVG(nr.score) as avgScore, COUNT(DISTINCT nr.note_id) as noteCount, COUNT(*) as totalRatings " +
            "FROM note_rating nr " +
            "INNER JOIN note n ON nr.note_id = n.id " +
            "WHERE n.user_id = #{creatorId}")
    java.util.Map<String, Object> selectCreatorRatingStats(@Param("creatorId") Long creatorId);
}
