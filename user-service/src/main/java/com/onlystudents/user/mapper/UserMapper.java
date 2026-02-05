package com.onlystudents.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);
    
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(String phone);
}
