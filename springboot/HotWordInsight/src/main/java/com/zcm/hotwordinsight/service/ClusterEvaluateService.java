package com.zcm.hotwordinsight.service;

import java.util.Map;

/**
 * 聚类评估服务接口
 */
public interface ClusterEvaluateService {

    /**
     * 计算聚类评估指标
     * @param dataType 数据类型(hotword/commodity)
     * @param k 聚类数量
     * @param clusters 聚类结果
     * @return 评估结果包含 SSE、轮廓系数等
     */
    Map<String, Object> evaluateClustering(String dataType, int k, Object clusters);

    /**
     * 使用肘部法则找到最优K值
     * @param dataType 数据类型
     * @param maxK 最大K值
     * @return 最优K值
     */
    int findOptimalK(String dataType, int maxK);

    /**
     * 计算SSE(簇内误差平方和)
     * @param dataPoints 数据点
     * @param clusterAssignments 聚类分配
     * @param centers 聚类中心
     * @return SSE值
     */
    double calculateSSE(double[][] dataPoints, int[] clusterAssignments, double[][] centers);

    /**
     * 计算轮廓系数
     * @param dataPoints 数据点
     * @param clusterAssignments 聚类分配
     * @return 轮廓系数
     */
    double calculateSilhouetteScore(double[][] dataPoints, int[] clusterAssignments);
}
