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

    @Select("SELECT * FROM note WHERE deleted = 0 AND status = 2 AND visibility != 4 ORDER BY hot_score DESC LIMIT #{limit}")
    List<Note> selectHotNotes(@Param("limit") Integer limit);

    @Select("SELECT * FROM note WHERE deleted = 0 AND status = 2 AND visibility != 4 ORDER BY publish_time DESC LIMIT #{limit}")
    List<Note> selectLatestNotes(@Param("limit") Integer limit);

    @Select("SELECT * FROM note WHERE user_id = #{userId} AND deleted = 0 AND status = 2 ORDER BY publish_time DESC LIMIT #{limit}")
    List<Note> selectUserLatestNotes(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT * FROM note WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<Note> selectUserNotes(@Param("userId") Long userId);

    @Update("UPDATE note SET view_count = view_count + 1 WHERE id = #{noteId}")
    int incrementViewCount(@Param("noteId") Long noteId);

    /**
     * 更新收藏数（设置为指定值）
     */
    @Update("UPDATE note SET favorite_count = favorite_count+1 WHERE id = #{noteId}")
    int updateFavoriteCount(@Param("noteId") Long noteId);

    @Update("UPDATE note SET favorite_count = favorite_count-1 WHERE id = #{noteId}")
    int dreaseFavoriteCount(@Param("noteId") Long noteId);

    /**
     * 更新分享数
     */
    @Update("UPDATE note SET share_count = share_count+1 WHERE id = #{noteId}")
    int updateShareCount(@Param("noteId") Long noteId);

    /**
     * 更新评分统计
     */
    @Update("UPDATE note SET average_rating = #{avgScore}, rating_count = rating_count+1 WHERE id = #{noteId}")
    int updateRatingStats(@Param("noteId") Long noteId, @Param("avgScore") Double avgScore);

    /**
     * 分页查询已发布的公开笔记（用于ES全量同步）
     * 只同步公开的笔记（visibility = 0），其他可见性级别的笔记不进入ES
     */
    @Select("SELECT * FROM note WHERE deleted = 0 AND status = 2 AND visibility != 4 ORDER BY id LIMIT #{offset}, #{limit}")
    List<Note> selectPublishedNotesByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 批量查询笔记
     */
    @Select("<script>" +
            "SELECT * FROM note WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    List<Note> selectListByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM note WHERE school_id = #{schoolId} AND deleted = 0 AND status = 2 AND visibility != 4 ORDER BY publish_time DESC LIMIT #{limit}")
    List<Note> selectNotesBySchoolId(@Param("schoolId") Long schoolId, @Param("limit") Integer limit);

    /**
     * 根据用户ID列表查询已发布笔记（分页）
     */
    @Select("<script>" +
            "SELECT * FROM note WHERE user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            " AND deleted = 0 AND status = 2 AND visibility != 4 " +
            "ORDER BY publish_time DESC LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Note> selectNotesByUserIds(@Param("userIds") List<Long> userIds, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT " +
            "COUNT(*) as totalNotes, " +
            "COALESCE(SUM(view_count), 0) as totalViews, " +
            "COALESCE(SUM(comment_count), 0) as totalComments, " +
            "COALESCE(SUM(favorite_count), 0) as totalCollects, " +
            "COALESCE(SUM(share_count), 0) as totalShares " +
            "FROM note WHERE user_id = #{creatorId} AND deleted = 0 AND status = 2")
    java.util.Map<String, Object> selectCreatorNoteStats(@Param("creatorId") Long creatorId);
}
