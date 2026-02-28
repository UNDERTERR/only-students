package com.onlystudents.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school")
public class School {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String province;

    private String city;

    private Integer population;

    private Integer notes;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
