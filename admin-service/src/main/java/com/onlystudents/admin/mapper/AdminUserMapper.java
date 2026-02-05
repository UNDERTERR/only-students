package com.onlystudents.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.admin.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("SELECT * FROM admin_user WHERE username = #{username} AND status = 1")
    AdminUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM admin_user WHERE email = #{email} AND status = 1")
    AdminUser selectByEmail(@Param("email") String email);

    @Update("UPDATE admin_user SET last_login_time = NOW(), last_login_ip = #{ip} WHERE id = #{userId}")
    int updateLoginInfo(@Param("userId") Long userId, @Param("ip") String ip);
}
