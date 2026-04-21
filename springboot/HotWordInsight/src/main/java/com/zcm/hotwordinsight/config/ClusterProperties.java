package com.zcm.hotwordinsight.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * KMeans聚类配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "cluster.kmeans")
public class ClusterProperties {

    /**
     * 特征向量维度
     */
    private int defaultDimension = 64;

    /**
     * 最大迭代次数
     */
    private int maxIterations = 100;

    /**
     * 收敛阈值
     */
    private double epsilon = 1e-6;

    /**
     * 随机种子
     */
    private int randomSeed = 42;
}
