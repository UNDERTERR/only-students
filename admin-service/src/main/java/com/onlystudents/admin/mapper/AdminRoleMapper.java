package com.onlystudents.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.admin.entity.AdminRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminRoleMapper extends BaseMapper<AdminRole> {

    @Select("SELECT * FROM admin_role WHERE code = #{roleCode}")
    AdminRole selectByRoleCode(@Param("roleCode") String roleCode);

    @Select("SELECT * FROM admin_role WHERE status = 1")
    List<AdminRole> selectAllActiveRoles();
}
