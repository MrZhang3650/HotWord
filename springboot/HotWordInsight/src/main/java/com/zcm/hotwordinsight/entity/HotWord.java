package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 热词实体类
 */
@Data
@TableName("hotword")
@Schema(description = "热词实体类")
public class HotWord {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 热词文本
     */
    @Schema(description = "热词文本")
    private String keyword;
    /**
     * 热度值
     */
    @Schema(description = "热度值")
    private Integer heat;
    /**
     * 搜索人数
     */
    @Schema(description = "搜索人数")
    @TableField("search_count")
    private Integer searchCount;
    /**
     * 相关商品数量
     */
    @Schema(description = "相关商品数量")
    @TableField("related_count")
    private Integer relatedCount;
    /**
     * 来源平台
     */
    @Schema(description = "来源平台")
    private String source;
    /**
     * 采集时间
     */
    @Schema(description = "采集时间")
    @TableField("collect_time")
    private Date collectTime;
}
