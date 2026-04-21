package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 聚类中心信息实体类
 */
@Data
@TableName("cluster_info")
@Schema(description = "聚类中心信息实体类")
public class ClusterInfo {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("cluster_id")
    @Schema(description = "聚类类别编号（如 0、1、2）")
    private Integer clusterId;

    @TableField("data_type")
    @Schema(description = "数据类型：hotword-热词 / commodity-商品")
    private String dataType;

    @TableField("center")
    @Schema(description = "聚类中心向量（TF-IDF/Word2Vec生成）")
    private String center;

    @TableField("size")
    @Schema(description = "该类别包含的数据条数")
    private Integer size;

    @TableField("label")
    @Schema(description = "聚类类别标签（如：服饰、电子、美妆）")
    private String label;

    @TableField("create_time")
    @Schema(description = "聚类生成时间")
    private Date createTime;
}
