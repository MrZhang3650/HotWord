package com.zcm.hotwordinsight.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 预处理结果实体类（用于预览）
 */
@Data
@Schema(description = "预处理结果实体类")
public class PreprocessResult {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关键词/商品标题")
    private String keyword;

    @Schema(description = "热度值/价格")
    private BigDecimal heat;

    @Schema(description = "搜索人数/销量")
    private Integer searchCount;

    @Schema(description = "相关商品数量/评价数")
    private Long relatedCount;

    @Schema(description = "来源平台")
    private String source;

    @Schema(description = "采集时间")
    private Date collectTime;

    @Schema(description = "商品编号")
    private String itemId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "评价数")
    private Integer reviewCount;

    @Schema(description = "卖家/店铺")
    private String seller;

    @Schema(description = "商品类别")
    private String category;
}
