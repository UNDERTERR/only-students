package com.onlystudents.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "裁剪图片请求")
public class CropImageRequest {
    
    @Schema(description = "原图片URL", required = true)
    @NotNull(message = "原图片URL不能为空")
    private String imageUrl;
    
    @Schema(description = "裁剪区域X坐标", required = true)
    @NotNull(message = "裁剪X坐标不能为空")
    private Integer x;
    
    @Schema(description = "裁剪区域Y坐标", required = true)
    @NotNull(message = "裁剪Y坐标不能为空")
    private Integer y;
    
    @Schema(description = "裁剪区域宽度", required = true)
    @NotNull(message = "裁剪宽度不能为空")
    private Integer width;
    
    @Schema(description = "裁剪区域高度", required = true)
    @NotNull(message = "裁剪高度不能为空")
    private Integer height;
    
    @Schema(description = "图片缩放比例", required = false)
    private Float scale = 1.0f;
}
