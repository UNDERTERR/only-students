package com.onlystudents.rating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.rating.entity.NoteFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoteFavoriteMapper extends BaseMapper<NoteFavorite> {
    
    /**
     * 查询用户是否已收藏笔记
     */
    @Select("SELECT * FROM note_favorite WHERE note_id = #{noteId} AND user_id = #{userId} LIMIT 1")
    NoteFavorite selectByNoteAndUser(@Param("noteId") Long noteId, @Param("userId") Long userId);
    
    /**
     * 查询笔记收藏数
     */
    @Select("SELECT COUNT(*) FROM note_favorite WHERE note_id = #{noteId}")
    Long countByNoteId(@Param("noteId") Long noteId);
    
    /**
     * 查询用户的收藏列表
     */
    @Select("SELECT * FROM note_favorite WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<NoteFavorite> selectListByUser(@Param("userId") Long userId);
    
    /**
     * 查询用户的收藏列表（按收藏夹筛选）
     */
    @Select("SELECT * FROM note_favorite WHERE user_id = #{userId} AND folder_id = #{folderId} ORDER BY created_at DESC")
    List<NoteFavorite> selectListByUserAndFolder(@Param("userId") Long userId, @Param("folderId") Long folderId);
    
    /**
     * 查询用户的收藏总数
     */
    @Select("SELECT COUNT(*) FROM note_favorite WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户指定收藏夹的收藏数量
     */
    @Select("SELECT COUNT(*) FROM note_favorite WHERE user_id = #{userId} AND folder_id = #{folderId}")
    Long countByUserAndFolder(@Param("userId") Long userId, @Param("folderId") Long folderId);
    
    /**
     * 清除收藏夹ID（删除收藏夹时调用）
     */
    @Update("UPDATE note_favorite SET folder_id = NULL WHERE folder_id = #{folderId} AND user_id = #{userId}")
    void clearFolderById(@Param("folderId") Long folderId, @Param("userId") Long userId);
    
    /**
     * 获取我的笔记被收藏的记录（分页）
     */
    @Select("SELECT nf.* FROM note_favorite nf " +
            "INNER JOIN note n ON nf.note_id = n.id " +
            "WHERE n.user_id = #{userId} " +
            "ORDER BY nf.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<NoteFavorite> selectMyNoteFavorites(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取我的笔记被收藏的未读数量
     */
    @Select("SELECT COUNT(*) FROM note_favorite nf " +
            "INNER JOIN note n ON nf.note_id = n.id " +
            "WHERE n.user_id = #{userId} AND (nf.is_read = 0 OR nf.is_read IS NULL)")
    Long countMyNoteFavoriteUnread(@Param("userId") Long userId);
    
    /**
     * 标记收藏为已读
     */
    @Update("UPDATE note_favorite nf " +
            "INNER JOIN note n ON nf.note_id = n.id " +
            "SET nf.is_read = 1 " +
            "WHERE nf.id = #{favoriteId} AND n.user_id = #{userId}")
    int markAsRead(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId);
}
