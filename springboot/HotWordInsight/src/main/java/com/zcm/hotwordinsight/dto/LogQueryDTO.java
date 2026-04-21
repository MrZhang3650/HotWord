package com.zcm.hotwordinsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日志查询DTO
 */
@Data
@Schema(description = "日志查询DTO")
public class LogQueryDTO {
    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    /**
     * 数据类型
     */
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private String operation;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;
}
