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
    
    @Select("SELECT * FROM comment WHERE note_id = #{noteId} AND deleted = 0 AND status = 1 AND parent_id = 0 ORDER BY is_top DESC, created_at DESC")
    List<Comment> selectRootCommentsByNoteId(Long noteId);
    
    @Select("SELECT * FROM comment WHERE root_id = #{rootId} AND deleted = 0 AND status = 1 ORDER BY created_at ASC")
    List<Comment> selectRepliesByRootId(Long rootId);
    
    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND deleted = 0 AND status = 1 ORDER BY created_at ASC")
    List<Comment> selectDirectRepliesByParentId(Long parentId);
    
    @Select("SELECT COUNT(*) FROM comment WHERE note_id = #{noteId} AND deleted = 0 AND status = 1")
    Integer countByNoteId(Long noteId);
    
    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(Long commentId);
    
    @Update("UPDATE comment SET reply_count = reply_count + 1 WHERE id = #{commentId}")
    int incrementReplyCount(Long commentId);
    
    @Select("<script>" +
            "SELECT * FROM comment WHERE deleted = 0 AND status = 1 " +
            "<if test='noteIds != null and noteIds.size() > 0'>AND note_id IN " +
            "<foreach collection='noteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> " +
            "AND user_id != #{userId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Comment> selectReceivedComments(@Param("userId") Long userId, @Param("noteIds") List<Long> noteIds, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND deleted = 0 AND status = 1 ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<Comment> selectSentComments(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM comment WHERE deleted = 0 AND status = 1 " +
            "<if test='noteIds != null and noteIds.size() > 0'>AND note_id IN " +
            "<foreach collection='noteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> " +
            "AND user_id != #{userId} " +
            "AND (is_read = 0 OR is_read IS NULL)" +
            "</script>")
    Integer countReceivedCommentsUnread(@Param("userId") Long userId, @Param("noteIds") List<Long> noteIds);
    
    @Update("UPDATE comment SET is_read = 1 WHERE id = #{commentId}")
    int markAsRead(@Param("commentId") Long commentId);
}
