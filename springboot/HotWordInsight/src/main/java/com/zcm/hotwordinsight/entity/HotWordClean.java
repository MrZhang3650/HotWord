package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 预处理后热词实体类
 */
@Data
@TableName("hotword_clean")
@Schema(description = "预处理后热词实体类")
public class HotWordClean {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 热词关键词
     */
    @Schema(description = "热词关键词")
    private String keyword;

    /**
     * 热度值（空值填充为0）
     */
    @Schema(description = "热度值")
    private Long heat;

    /**
     * 搜索次数（非负）
     */
    @Schema(description = "搜索次数")
    @TableField("search_count")
    private Integer searchCount;

    /**
     * 相关数量（空值填充为0）
     */
    @Schema(description = "相关数量")
    @TableField("related_count")
    private Long relatedCount;

    /**
     * 数据来源（规范为京东/淘宝/拼多多）
     */
    @Schema(description = "数据来源")
    private String source;

    /**
     * 采集时间
     */
    @Schema(description = "采集时间")
    @TableField("collect_time")
    private Date collectTime;

    /**
     * 数据清洗时间
     */
    @Schema(description = "数据清洗时间")
    @TableField("clean_time")
    private Date cleanTime;
}
