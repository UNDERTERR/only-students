package com.onlystudents.rating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建收藏夹请求
 */
@Data
public class CreateFolderRequest {
    
    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;
}
