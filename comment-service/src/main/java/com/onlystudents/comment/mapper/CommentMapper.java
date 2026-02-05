package com.onlystudents.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.comment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT * FROM comment WHERE note_id = #{noteId} AND status = 1 AND parent_id = 0 ORDER BY is_top DESC, created_at DESC")
    List<Comment> selectRootCommentsByNoteId(Long noteId);
    
    @Select("SELECT * FROM comment WHERE root_id = #{rootId} AND status = 1 ORDER BY created_at ASC")
    List<Comment> selectRepliesByRootId(Long rootId);
    
    @Select("SELECT COUNT(*) FROM comment WHERE note_id = #{noteId} AND status = 1")
    Integer countByNoteId(Long noteId);
    
    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(Long commentId);
    
    @Update("UPDATE comment SET reply_count = reply_count + 1 WHERE id = #{commentId}")
    int incrementReplyCount(Long commentId);
}
