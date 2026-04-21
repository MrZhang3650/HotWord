package com.zcm.hotwordinsight.service.impl;

import com.zcm.hotwordinsight.config.ClusterProperties;
import com.zcm.hotwordinsight.entity.ClusterInfo;
import com.zcm.hotwordinsight.entity.CommodityClean;
import com.zcm.hotwordinsight.entity.HotWordClean;
import com.zcm.hotwordinsight.mapper.ClusterInfoMapper;
import com.zcm.hotwordinsight.mapper.CommodityCleanMapper;
import com.zcm.hotwordinsight.mapper.HotWordCleanMapper;
import com.zcm.hotwordinsight.service.ClusterEvaluateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 聚类评估服务实现类
 */
@Slf4j
@Service
public class ClusterEvaluateServiceImpl implements ClusterEvaluateService {

    @Resource
    private HotWordCleanMapper hotWordCleanMapper;

    @Resource
    private CommodityCleanMapper commodityCleanMapper;

    @Resource
    private ClusterInfoMapper clusterInfoMapper;

    @Resource
    private ClusterProperties clusterProperties;

    @Override
    public Map<String, Object> evaluateClustering(String dataType, int k, Object clusters) {
        Map<String, Object> result = new HashMap<>();
        result.put("k", k);
        result.put("dataType", dataType);

        if (clusters instanceof List<?>) {
            List<?> clusterList = (List<?>) clusters;
            result.put("clusterCount", clusterList.size());
        }

        return result;
    }

    @Override
    public int findOptimalK(String dataType, int maxK) {
        log.info("开始寻找最优K值，数据类型: {}, 最大K: {}", dataType, maxK);

        List<double[]> dataPoints = loadDataPoints(dataType);
        if (dataPoints.isEmpty()) {
            log.warn("没有可用的数据点，返回默认K值3");
            return 3;
        }

        maxK = Math.min(maxK, Math.min(10, dataPoints.size() / 2));
        if (maxK < 2) {
            maxK = 2;
        }

        List<Double> sseList = new ArrayList<>();
        List<Integer> kValues = new ArrayList<>();

        for (int k = 2; k <= maxK; k++) {
            KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(
                    k,
                    clusterProperties.getMaxIterations()
            );

            List<DoublePoint> points = new ArrayList<>();
            for (double[] dp : dataPoints) {
                points.add(new DoublePoint(dp));
            }

            List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

            double sse = calculateSSEFromClusters(points, clusters);
            sseList.add(sse);
            kValues.add(k);

            log.info("K={} 时 SSE={}", k, sse);
        }

        int elbowK = findElbowPoint(sseList, kValues);
        double silhouette = calculateAverageSilhouette(dataPoints, elbowK);

        log.info("肘部法则找到的最优K: {}, 轮廓系数: {}", elbowK, silhouette);

        return elbowK;
    }

    private List<double[]> loadDataPoints(String dataType) {
        List<double[]> dataPoints = new ArrayList<>();

        if ("hotword".equals(dataType)) {
            List<HotWordClean> dataList = hotWordCleanMapper.selectList(null);

            double maxHeat = 1, maxSearchCount = 1, maxRelatedCount = 1;
            for (HotWordClean hw : dataList) {
                if (hw.getHeat() != null && hw.getHeat() > maxHeat) maxHeat = hw.getHeat();
                if (hw.getSearchCount() != null && hw.getSearchCount() > maxSearchCount) maxSearchCount = hw.getSearchCount();
                if (hw.getRelatedCount() != null && hw.getRelatedCount() > maxRelatedCount) maxRelatedCount = hw.getRelatedCount();
            }

            for (HotWordClean hw : dataList) {
                double[] vector = new double[6];
                vector[0] = normalizeText(hw.getKeyword(), 3);
                vector[1] = normalizeText(hw.getSource(), 1);
                vector[2] = (hw.getHeat() != null) ? hw.getHeat() / maxHeat : 0;
                vector[3] = (hw.getSearchCount() != null) ? hw.getSearchCount() / maxSearchCount : 0;
                vector[4] = (hw.getRelatedCount() != null) ? hw.getRelatedCount() / maxRelatedCount : 0;
                vector[5] = 1.0;
                dataPoints.add(vector);
            }
        } else if ("commodity".equals(dataType)) {
            List<CommodityClean> dataList = commodityCleanMapper.selectList(null);

            double maxPrice = 1, maxSales = 1, maxReviewCount = 1;
            for (CommodityClean c : dataList) {
                if (c.getPrice() != null && c.getPrice().doubleValue() > maxPrice) {
                    maxPrice = c.getPrice().doubleValue();
                }
                if (c.getSales() != null && c.getSales() > maxSales) maxSales = c.getSales();
                if (c.getReviewCount() != null && c.getReviewCount() > maxReviewCount) {
                    maxReviewCount = c.getReviewCount();
                }
            }

            for (CommodityClean c : dataList) {
                double[] vector = new double[6];
                vector[0] = normalizeText(c.getTitle(), 3);
                vector[1] = normalizeText(c.getSource(), 1);
                vector[2] = (c.getPrice() != null) ? c.getPrice().doubleValue() / maxPrice : 0;
                vector[3] = (c.getSales() != null) ? c.getSales() / maxSales : 0;
                vector[4] = (c.getReviewCount() != null) ? c.getReviewCount() / maxReviewCount : 0;
                vector[5] = 1.0;
                dataPoints.add(vector);
            }
        }

        return dataPoints;
    }

    private double normalizeText(String text, double factor) {
        if (text == null || text.isEmpty()) return 0;
        double sum = 0;
        for (char c : text.toCharArray()) {
            sum += c;
        }
        return (sum / text.length()) / 1000.0 * factor;
    }

    private int findElbowPoint(List<Double> sseList, List<Integer> kValues) {
        if (sseList.size() < 2) {
            return kValues.get(0);
        }

        double[] sseArray = new double[sseList.size()];
        for (int i = 0; i < sseList.size(); i++) {
            sseArray[i] = sseList.get(i);
        }

        double[] gradients = new double[sseArray.length - 1];
        for (int i = 0; i < gradients.length; i++) {
            gradients[i] = sseArray[i] - sseArray[i + 1];
        }

        int elbowIndex = 0;
        double maxGradient = gradients[0];
        for (int i = 1; i < gradients.length; i++) {
            if (gradients[i] > maxGradient) {
                maxGradient = gradients[i];
                elbowIndex = i;
            }
        }

        return kValues.get(elbowIndex);
    }

    private double calculateSSEFromClusters(List<DoublePoint> points,
            List<CentroidCluster<DoublePoint>> clusters) {
        double sse = 0;

        for (CentroidCluster<DoublePoint> cluster : clusters) {
            double[] center = cluster.getCenter().getPoint();
            for (DoublePoint point : cluster.getPoints()) {
                double dist = 0;
                for (int i = 0; i < center.length; i++) {
                    double diff = point.getPoint()[i] - center[i];
                    dist += diff * diff;
                }
                sse += dist;
            }
        }

        return sse;
    }

    private double calculateAverageSilhouette(List<double[]> dataPoints, int k) {
        if (dataPoints.size() < 2) {
            return 0;
        }

        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(
                k,
                clusterProperties.getMaxIterations()
        );

        List<DoublePoint> points = new ArrayList<>();
        for (double[] dp : dataPoints) {
            points.add(new DoublePoint(dp));
        }

        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

        int[] assignments = new int[points.size()];
        for (int i = 0; i < clusters.size(); i++) {
            for (DoublePoint p : clusters.get(i).getPoints()) {
                int idx = points.indexOf(p);
                if (idx >= 0) {
                    assignments[idx] = i;
                }
            }
        }

        double totalSilhouette = 0;
        int count = 0;

        for (int i = 0; i < points.size(); i++) {
            double a = averageIntraClusterDistance(points.get(i).getPoint(), assignments[i], points, clusters);
            double b = averageNearestClusterDistance(points.get(i).getPoint(), assignments[i], points, clusters);

            if (b > 0 || a > 0) {
                totalSilhouette += (b - a) / Math.max(b, a);
                count++;
            }
        }

        return count > 0 ? totalSilhouette / count : 0;
    }

    private double averageIntraClusterDistance(double[] point, int clusterId,
            List<DoublePoint> allPoints, List<CentroidCluster<DoublePoint>> clusters) {
        double totalDist = 0;
        int count = 0;

        for (DoublePoint p : clusters.get(clusterId).getPoints()) {
            if (p.getPoint() != point) {
                totalDist += euclideanDistance(point, p.getPoint());
                count++;
            }
        }

        return count > 0 ? totalDist / count : 0;
    }

    private double averageNearestClusterDistance(double[] point, int clusterId,
            List<DoublePoint> allPoints, List<CentroidCluster<DoublePoint>> clusters) {
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < clusters.size(); i++) {
            if (i != clusterId) {
                double dist = euclideanDistance(point, clusters.get(i).getCenter().getPoint());
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }

        return minDist == Double.MAX_VALUE ? 0 : minDist;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    @Override
    public double calculateSSE(double[][] dataPoints, int[] clusterAssignments, double[][] centers) {
        double sse = 0;

        for (int i = 0; i < dataPoints.length; i++) {
            int clusterId = clusterAssignments[i];
            double dist = 0;
            for (int j = 0; j < dataPoints[i].length; j++) {
                double diff = dataPoints[i][j] - centers[clusterId][j];
                dist += diff * diff;
            }
            sse += dist;
        }

        return sse;
    }

    @Override
    public double calculateSilhouetteScore(double[][] dataPoints, int[] clusterAssignments) {
        int n = dataPoints.length;
        if (n < 2) return 0;

        int k = 0;
        for (int assignment : clusterAssignments) {
            k = Math.max(k, assignment + 1);
        }

        @SuppressWarnings("unchecked")
        List<Integer>[] clusters = new List[k];
        for (int i = 0; i < k; i++) {
            clusters[i] = new ArrayList<>();
        }
        for (int i = 0; i < n; i++) {
            clusters[clusterAssignments[i]].add(i);
        }

        double totalSilhouette = 0;
        int count = 0;

        for (int i = 0; i < n; i++) {
            int ownCluster = clusterAssignments[i];
            double a = 0;
            if (clusters[ownCluster].size() > 1) {
                double sum = 0;
                for (int j : clusters[ownCluster]) {
                    if (j != i) {
                        sum += euclideanDistance(dataPoints[i], dataPoints[j]);
                    }
                }
                a = sum / (clusters[ownCluster].size() - 1);
            }

            double b = Double.MAX_VALUE;
            for (int c = 0; c < k; c++) {
                if (c != ownCluster && !clusters[c].isEmpty()) {
                    double sum = 0;
                    for (int j : clusters[c]) {
                        sum += euclideanDistance(dataPoints[i], dataPoints[j]);
                    }
                    double avg = sum / clusters[c].size();
                    b = Math.min(b, avg);
                }
            }

            if (b > 0 || a > 0) {
                totalSilhouette += (b - a) / Math.max(b, a);
                count++;
            }
        }

        return count > 0 ? totalSilhouette / count : 0;
    }
}
