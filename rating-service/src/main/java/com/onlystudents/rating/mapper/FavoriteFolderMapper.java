package com.onlystudents.rating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.rating.entity.FavoriteFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {
    
    /**
     * 查询用户的收藏夹列表
     */
    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} ORDER BY sort_order ASC, created_at DESC")
    List<FavoriteFolder> selectListByUser(@Param("userId") Long userId);
}
