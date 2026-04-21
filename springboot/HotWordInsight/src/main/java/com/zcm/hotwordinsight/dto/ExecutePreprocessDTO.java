package com.zcm.hotwordinsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 执行预处理DTO
 */
@Data
@Schema(description = "执行预处理DTO")
public class ExecutePreprocessDTO {
    /**
     * 数据类型（hotword/commodity）
     */
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 处理步骤
     */
    @Schema(description = "处理步骤")
    private String[] steps;
}
