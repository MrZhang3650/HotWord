package com.zcm.hotwordinsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预处理预览DTO
 */
@Data
@Schema(description = "预处理预览DTO")
public class PreviewDTO {
    /**
     * 数据类型
     */
    @Schema(description = "数据类型")
    private String dataType;
}
