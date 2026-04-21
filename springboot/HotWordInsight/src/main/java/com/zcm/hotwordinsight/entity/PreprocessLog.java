package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 预处理日志实体类
 */
@Data
@TableName("preprocess_log")
@Schema(description = "预处理日志实体类")
public class PreprocessLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 数据类型（hotword/commodity）
     */
    @Schema(description = "数据类型")
    @TableField("data_type")
    private String dataType;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private String operation;

    /**
     * 总处理记录数
     */
    @Schema(description = "总处理记录数")
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 清洗记录数
     */
    @Schema(description = "清洗记录数")
    @TableField("clean_count")
    private Integer cleanCount;

    /**
     * 剔除异常数
     */
    @Schema(description = "剔除异常数")
    @TableField("outlier_count")
    private Integer outlierCount;

    /**
     * 向量化完成数
     */
    @Schema(description = "向量化完成数")
    @TableField("vector_count")
    private Integer vectorCount;

    /**
     * 处理状态（success/failed）
     */
    @Schema(description = "处理状态")
    private String status;

    /**
     * 日志详细信息
     */
    @Schema(description = "日志详细信息")
    private String message;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;
}
