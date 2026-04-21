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
 * 预处理后商品实体类
 */
@Data
@TableName("commodity_clean")
@Schema(description = "预处理后商品实体类")
public class CommodityClean {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    @TableField("item_id")
    private String itemId;

    /**
     * 商品标题
     */
    @Schema(description = "商品标题")
    private String title;

    /**
     * 商品价格（空值填充为0）
     */
    @Schema(description = "商品价格")
    private BigDecimal price;

    /**
     * 销量（非负）
     */
    @Schema(description = "销量")
    private Integer sales;

    /**
     * 评论数（非负）
     */
    @Schema(description = "评论数")
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 卖家名称（空值填充为空字符串）
     */
    @Schema(description = "卖家名称")
    private String seller;

    /**
     * 商品分类（空值填充为空字符串）
     */
    @Schema(description = "商品分类")
    private String category;

    /**
     * 品牌（空值填充为空字符串）
     */
    @Schema(description = "品牌")
    private String brand;

    /**
     * 商品链接（空值填充为空字符串）
     */
    @Schema(description = "商品链接")
    private String link;

    /**
     * 关联热词
     */
    @Schema(description = "关联热词")
    private String keyword;

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
