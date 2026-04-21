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
 * 商品实体类
 */
@Data
@TableName("commodity")
@Schema(description = "商品类")
public class Commodity {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 商品ID（平台唯一）
     */
    @Schema(description = "商品ID（平台唯一）")
    private String itemId;
    /**
     * 商品标题
     */
    @Schema(description = "商品标题")
    private String title;
    /**
     * 价格
     */
    @Schema(description = "价格")
    private BigDecimal price;
    /**
     * 销量
     */
    @Schema(description = "销量")
    private Integer sales;
    /**
     * 评价数
     */
    @Schema(description = "评价数")
    private Integer reviewCount;
    /**
     * 卖家/店铺
     */
    @Schema(description = "卖家/店铺")
    private String seller;
    /**
     * 商品类别
     */
    @Schema(description = "商品类别")
    private String category;
    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private String brand;
    /**
     * 商品链接
     */
    @Schema(description = "商品链接")
    private String link;
    /**
     * 关联的热词（采集时使用的关键词）
     */
    @Schema(description = "关联的热词（采集时使用的关键词）")
    private String keyword;
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
