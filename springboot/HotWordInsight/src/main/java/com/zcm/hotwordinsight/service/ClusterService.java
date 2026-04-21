package com.zcm.hotwordinsight.service;

import java.util.List;
import java.util.Map;

/**
 * 聚类服务接口
 */
public interface ClusterService {

    /**
     * 执行聚类分析
     * @param dataType 数据类型：hotword/commodity
     * @param k 聚类数量
     * @return 聚类结果
     */
    Map<String, Object> runClustering(String dataType, int k);

    /**
     * 获取聚类结果列表
     * @param dataType 数据类型
     * @return 聚类结果列表
     */
    List<Map<String, Object>> getClusteringResult(String dataType);

    /**
     * 获取聚类图表数据
     * @param dataType 数据类型
     * @return 图表数据
     */
    Map<String, Object> getClusteringChartData(String dataType);

    /**
     * 清空历史聚类数据
     * @param dataType 数据类型
     * @return 是否成功
     */
    boolean clearClusteringData(String dataType);

    /**
     * 获取最佳K值
     * @param dataType 数据类型
     * @param maxK 最大K值
     * @return 最佳K值和相关分析数据
     */
    Map<String, Object> getOptimalK(String dataType, int maxK);

    /**
     * 根据关键词生成特征向量
     * @param keyword 关键词
     * @param dimension 向量维度
     * @return 特征向量
     */
    double[] generateFeatureVector(String keyword, int dimension);
}
