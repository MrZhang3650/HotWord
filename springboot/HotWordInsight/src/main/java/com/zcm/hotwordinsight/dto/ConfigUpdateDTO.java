package com.zcm.hotwordinsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 配置更新DTO
 */
@Data
@Schema(description = "配置更新DTO")
public class ConfigUpdateDTO {
    /**
     * 异常值阈值
     */
    @Schema(description = "异常值阈值")
    private Double outlierThreshold;

    /**
     * 标准化最小值
     */
    @Schema(description = "标准化最小值")
    private Double normalizeMin;

    /**
     * 标准化最大值
     */
    @Schema(description = "标准化最大值")
    private Double normalizeMax;
}
