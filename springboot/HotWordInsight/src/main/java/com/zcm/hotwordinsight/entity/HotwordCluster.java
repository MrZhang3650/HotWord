package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 热词聚类结果实体类
 */
@Data
@TableName("hotword_cluster")
@Schema(description = "热词聚类结果实体类")
public class HotwordCluster {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("hotword_id")
    @Schema(description = "热词ID，关联hotword表")
    private Long hotwordId;

    @TableField("cluster_id")
    @Schema(description = "簇编号")
    private Integer clusterId;

    @TableField("distance_to_center")
    @Schema(description = "到簇中心的距离")
    private Double distanceToCenter;

    @TableField("algorithm_params")
    @Schema(description = "聚类参数（如k值、迭代次数）")
    private String algorithmParams;

    @TableField("create_time")
    @Schema(description = "聚类时间")
    private Date createTime;

    @TableField("data_type")
    @Schema(description = "数据类型：hotword")
    private String dataType;
}
