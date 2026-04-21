package com.zcm.hotwordinsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导出DTO
 */
@Data
@Schema(description = "导出DTO")
public class ExportDTO {
    /**
     * 数据类型
     */
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 导出格式（excel/csv）
     */
    @Schema(description = "导出格式")
    private String format;
}
