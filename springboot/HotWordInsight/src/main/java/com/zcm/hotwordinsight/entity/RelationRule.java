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
 * 关联规则实体类
 */
@Data
@TableName("relation_rule")
@Schema(description = "关联规则实体类")
public class RelationRule {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("antecedent")
    @Schema(description = "前项（A -> B 中的A）")
    private String antecedent;

    @TableField("consequent")
    @Schema(description = "后项（A -> B 中的B）")
    private String consequent;

    @TableField("support")
    @Schema(description = "支持度")
    private BigDecimal support;

    @TableField("confidence")
    @Schema(description = "置信度")
    private BigDecimal confidence;

    @TableField("lift")
    @Schema(description = "提升度")
    private BigDecimal lift;

    @TableField("data_type")
    @Schema(description = "数据类型 hotword/commodity")
    private String dataType;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private Date createTime;
}
