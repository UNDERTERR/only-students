package com.onlystudents.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);
    
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(String phone);
    
    /**
     * 分页查询用户（用于ES全量同步）
     * 只查询状态正常的用户 (status = 1)
     */
    @Select("SELECT * FROM user WHERE status = 1 ORDER BY id LIMIT #{offset}, #{limit}")
    List<User> selectUsersByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 搜索用户（MySQL LIKE 查询）
     */
    @Select("<script>" +
            "SELECT * FROM user " +
            "WHERE status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "OR bio LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='educationLevel != null'>" +
            "AND education_level = #{educationLevel} " +
            "</if>" +
            "<if test='isCreator != null'>" +
            "AND is_creator = #{isCreator} " +
            "</if>" +
            "ORDER BY follower_count DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<User> searchUsers(@Param("keyword") String keyword, 
                           @Param("educationLevel") Integer educationLevel,
                           @Param("isCreator") Integer isCreator,
                           @Param("offset") Integer offset, 
                           @Param("limit") Integer limit);
}
