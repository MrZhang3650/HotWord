package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品聚类结果实体类
 */
@Data
@TableName("commodity_cluster")
@Schema(description = "商品聚类结果实体类")
public class CommodityCluster {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("cluster_id")
    @Schema(description = "聚类分组ID")
    private Integer clusterId;

    @TableField("item_id")
    @Schema(description = "商品唯一ID")
    private String itemId;

    @TableField("title")
    @Schema(description = "商品标题")
    private String title;

    @TableField("keyword")
    @Schema(description = "关联热词")
    private String keyword;

    @TableField("price")
    @Schema(description = "价格")
    private BigDecimal price;

    @TableField("sales")
    @Schema(description = "销量")
    private Integer sales;

    @TableField("source")
    @Schema(description = "来源平台")
    private String source;

    @TableField("cluster_time")
    @Schema(description = "聚类时间")
    private Date clusterTime;
}
