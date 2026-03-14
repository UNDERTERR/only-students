package com.onlystudents.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_role")
public class AdminRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String roleName;

    @TableField("code")
    private String roleCode;

    private String description;

    private String permissions;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
