package com.onlystudents.rating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.rating.entity.NoteFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
