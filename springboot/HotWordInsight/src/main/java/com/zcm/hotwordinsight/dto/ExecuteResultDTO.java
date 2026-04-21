package com.zcm.hotwordinsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 执行结果DTO
 */
@Data
@Schema(description = "执行结果DTO")
public class ExecuteResultDTO {
    /**
     * 总处理记录数
     */
    @Schema(description = "总处理记录数")
    private Integer totalCount;

    /**
     * 清洗结果
     */
    @Schema(description = "清洗结果")
    private StepResult cleanResult;

    /**
     * 分词结果
     */
    @Schema(description = "分词结果")
    private StepResult segmentResult;

    /**
     * 去停用词结果
     */
    @Schema(description = "去停用词结果")
    private StopwordsResult stopwordsResult;

    /**
     * 异常剔除结果
     */
    @Schema(description = "异常剔除结果")
    private OutlierResult outlierResult;

    /**
     * 标准化结果
     */
    @Schema(description = "标准化结果")
    private NormalizeResult normalizeResult;

    /**
     * 向量化结果
     */
    @Schema(description = "向量化结果")
    private VectorizeResult vectorizeResult;

    /**
     * 步骤结果
     */
    @Data
    @Schema(description = "步骤结果")
    public static class StepResult {
        /**
         * 成功数
         */
        @Schema(description = "成功数")
        private Integer successCount;

        /**
         * 失败数
         */
        @Schema(description = "失败数")
        private Integer failCount;
    }

    /**
     * 去停用词结果
     */
    @Data
    @Schema(description = "去停用词结果")
    public static class StopwordsResult {
        /**
         * 成功数
         */
        @Schema(description = "成功数")
        private Integer successCount;

        /**
         * 移除数
         */
        @Schema(description = "移除数")
        private Integer removedCount;
    }

    /**
     * 异常剔除结果
     */
    @Data
    @Schema(description = "异常剔除结果")
    public static class OutlierResult {
        /**
         * 成功数
         */
        @Schema(description = "成功数")
        private Integer successCount;

        /**
         * 移除数
         */
        @Schema(description = "移除数")
        private Integer removedCount;
    }

    /**
     * 标准化结果
     */
    @Data
    @Schema(description = "标准化结果")
    public static class NormalizeResult {
        /**
         * 成功数
         */
        @Schema(description = "成功数")
        private Integer successCount;

        /**
         * 范围
         */
        @Schema(description = "范围")
        private Range range;

        /**
         * 范围
         */
        @Data
        @Schema(description = "范围")
        public static class Range {
            /**
             * 最小值
             */
            @Schema(description = "最小值")
            private Double min;

            /**
             * 最大值
             */
            @Schema(description = "最大值")
            private Double max;
        }
    }

    /**
     * 向量化结果
     */
    @Data
    @Schema(description = "向量化结果")
    public static class VectorizeResult {
        /**
         * 成功数
         */
        @Schema(description = "成功数")
        private Integer successCount;

        /**
         * 维度
         */
        @Schema(description = "维度")
        private Integer dimension;
    }
}
