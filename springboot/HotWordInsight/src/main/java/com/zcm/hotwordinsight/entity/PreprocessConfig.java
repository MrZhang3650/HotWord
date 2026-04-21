package com.zcm.hotwordinsight.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预处理配置实体类
 */
@Data
@Schema(description = "预处理配置实体类")
public class PreprocessConfig {
    /**
     * 异常值阈值（标准差倍数）
     */
    @Schema(description = "异常值阈值")
    private Double outlierThreshold = 3.0;

    /**
     * 标准化最小值
     */
    @Schema(description = "标准化最小值")
    private Double normalizeMin = 0.0;

    /**
     * 标准化最大值
     */
    @Schema(description = "标准化最大值")
    private Double normalizeMax = 1.0;

    /**
     * 向量化维度
     */
    @Schema(description = "向量化维度")
    private Integer vectorDimension = 128;
}
