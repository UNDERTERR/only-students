package com.onlystudents.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.user.entity.UserDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserDeviceMapper extends BaseMapper<UserDevice> {
    
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND status = 1 ORDER BY login_time DESC")
    List<UserDevice> selectActiveDevicesByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM user_device WHERE user_id = #{userId} AND status = 1")
    int countActiveDevices(Long userId);
    
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND device_id = #{deviceId}")
    UserDevice selectByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
}
