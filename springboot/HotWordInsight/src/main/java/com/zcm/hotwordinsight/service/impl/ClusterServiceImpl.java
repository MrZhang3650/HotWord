package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcm.hotwordinsight.config.ClusterProperties;
import com.zcm.hotwordinsight.entity.ClusterInfo;
import com.zcm.hotwordinsight.entity.CommodityClean;
import com.zcm.hotwordinsight.entity.CommodityCluster;
import com.zcm.hotwordinsight.entity.HotWordClean;
import com.zcm.hotwordinsight.entity.HotwordCluster;
import com.zcm.hotwordinsight.mapper.ClusterInfoMapper;
import com.zcm.hotwordinsight.mapper.CommodityCleanMapper;
import com.zcm.hotwordinsight.mapper.CommodityClusterMapper;
import com.zcm.hotwordinsight.mapper.HotWordCleanMapper;
import com.zcm.hotwordinsight.mapper.HotwordClusterMapper;
import com.zcm.hotwordinsight.service.ClusterEvaluateService;
import com.zcm.hotwordinsight.service.ClusterService;
import com.zcm.hotwordinsight.service.DataCleanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 聚类服务实现类 - 优化版
 * 支持热词和商品独立聚类，自动选择最优K值，输出评估指标
 */
@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {

    @Resource
    private HotWordCleanMapper hotWordCleanMapper;

    @Resource
    private CommodityCleanMapper commodityCleanMapper;

    @Resource
    private HotwordClusterMapper hotwordClusterMapper;

    @Resource
    private CommodityClusterMapper commodityClusterMapper;

    @Resource
    private ClusterInfoMapper clusterInfoMapper;

    @Resource
    private ClusterProperties clusterProperties;

    @Resource
    private DataCleanService dataCleanService;

    @Resource
    private ClusterEvaluateService clusterEvaluateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> runClustering(String dataType, int k) {
        if (dataType == null || dataType.isEmpty()) {
            throw new IllegalArgumentException("数据类型不能为空");
        }

        log.info("开始执行{}聚类，自动K值选择", dataType);

        if ("hotword".equals(dataType)) {
            return runHotWordClustering(k);
        } else if ("commodity".equals(dataType)) {
            return runCommodityClustering(k);
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + dataType);
        }
    }

    /**
     * 热词聚类流程
     */
    private Map<String, Object> runHotWordClustering(int userK) {
        Map<String, Object> result = new HashMap<>();

        // 幂等性处理：清空热词历史聚类数据
        log.info("幂等性处理：清空热词历史聚类数据");
        hotwordClusterMapper.delete(null);

        // 步骤0: 清洗数据
        log.info("步骤0: 清洗热词数据");
        Map<String, Object> cleanResult = dataCleanService.cleanHotWordData();
        if (!(Boolean) cleanResult.getOrDefault("success", false)) {
            result.put("success", false);
            result.put("message", "热词数据清洗失败");
            return result;
        }

        // 步骤1: 读取hotword_clean数据
        List<HotWordClean> dataList = hotWordCleanMapper.selectList(null);
        if (dataList == null || dataList.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有可聚类的热词数据");
            return result;
        }
        log.info("读取到{}条热词数据", dataList.size());

        // 步骤2: 自动选择最优K值
        int optimalK = userK > 0 ? userK : clusterEvaluateService.findOptimalK("hotword", 10);
        log.info("热词聚类最优K值: {}", optimalK);

        // 步骤3: 计算数值特征归一化参数
        double maxHeat = 1, maxSearchCount = 1, maxRelatedCount = 1;
        for (HotWordClean hw : dataList) {
            if (hw.getHeat() != null && hw.getHeat() > maxHeat) maxHeat = hw.getHeat();
            if (hw.getSearchCount() != null && hw.getSearchCount() > maxSearchCount) maxSearchCount = hw.getSearchCount();
            if (hw.getRelatedCount() != null && hw.getRelatedCount() > maxRelatedCount) maxRelatedCount = hw.getRelatedCount();
        }

        // 步骤4: 生成特征向量
        int dimension = clusterProperties.getDefaultDimension();
        List<DoublePoint> points = new ArrayList<>();
        Map<Integer, HotWordClean> pointToData = new HashMap<>();

        for (int i = 0; i < dataList.size(); i++) {
            HotWordClean hw = dataList.get(i);
            double[] vector = generateHotWordFeatureVector(hw, dimension, maxHeat, maxSearchCount, maxRelatedCount);
            DoublePoint point = new DoublePoint(vector);
            points.add(point);
            pointToData.put(points.indexOf(point), hw);
        }

        // 步骤5: 执行KMeans++聚类
        List<CentroidCluster<DoublePoint>> clusterList = performClustering(points, optimalK);

        // 步骤6: 计算评估指标
        double sse = calculateSSE(points, clusterList);
        double silhouette = calculateSilhouette(points, clusterList);

        log.info("热词聚类完成: K={}, SSE={}, 轮廓系数={}", optimalK, sse, silhouette);

        // 步骤7: 保存聚类结果
        List<HotwordCluster> clusterResults = new ArrayList<>();
        List<ClusterInfo> clusterInfos = new ArrayList<>();

        for (int i = 0; i < clusterList.size(); i++) {
            CentroidCluster<DoublePoint> cluster = clusterList.get(i);
            List<DoublePoint> clusterPoints = cluster.getPoints();
            double[] center = cluster.getCenter().getPoint();

            ClusterInfo info = new ClusterInfo();
            info.setClusterId(i + 1);
            info.setDataType("hotword");
            info.setCenter(arrayToString(center));
            info.setSize(clusterPoints.size());
            info.setLabel("热词类别" + (i + 1));
            info.setCreateTime(new Date());
            clusterInfos.add(info);

            for (DoublePoint p : clusterPoints) {
                HotWordClean hw = pointToData.get(points.indexOf(p));
                if (hw != null) {
                    HotwordCluster hc = new HotwordCluster();
                    hc.setHotwordId(hw.getId());
                    hc.setClusterId(i + 1);
                    hc.setDistanceToCenter(computeDistance(p.getPoint(), center));
                    hc.setAlgorithmParams("k=" + optimalK + ",dim=" + dimension + ",sse=" + sse + ",sil=" + silhouette);
                    hc.setCreateTime(new Date());
                    hc.setDataType("hotword");
                    clusterResults.add(hc);
                }
            }
        }

        // 步骤8: 清空并写入hotword_cluster
        hotwordClusterMapper.delete(null);
        clusterInfoMapper.delete(null);
        for (HotwordCluster hc : clusterResults) {
            hotwordClusterMapper.insert(hc);
        }
        for (ClusterInfo info : clusterInfos) {
            clusterInfoMapper.insert(info);
        }

        result.put("success", true);
        result.put("message", "热词聚类完成");
        result.put("dataType", "hotword");
        result.put("optimalK", optimalK);
        result.put("sse", sse);
        result.put("silhouetteScore", silhouette);
        result.put("totalCount", dataList.size());
        result.put("clusterCount", optimalK);
        result.put("clusters", clusterInfos.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("clusterId", c.getClusterId());
            m.put("size", c.getSize());
            m.put("label", c.getLabel());
            return m;
        }).toList());

        return result;
    }

    /**
     * 商品聚类流程
     */
    private Map<String, Object> runCommodityClustering(int userK) {
        Map<String, Object> result = new HashMap<>();

        // 幂等性处理：清空商品历史聚类数据
        log.info("幂等性处理：清空商品历史聚类数据");
        commodityClusterMapper.delete(null);

        // 步骤0: 清洗数据
        log.info("步骤0: 清洗商品数据");
        Map<String, Object> cleanResult = dataCleanService.cleanCommodityData();
        if (!(Boolean) cleanResult.getOrDefault("success", false)) {
            result.put("success", false);
            result.put("message", "商品数据清洗失败");
            return result;
        }

        // 步骤1: 读取commodity_clean数据
        List<CommodityClean> dataList = commodityCleanMapper.selectList(null);
        if (dataList == null || dataList.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有可聚类的商品数据");
            return result;
        }
        log.info("读取到{}条商品数据", dataList.size());

        // 步骤2: 自动选择最优K值
        int optimalK = userK > 0 ? userK : clusterEvaluateService.findOptimalK("commodity", 10);
        log.info("商品聚类最优K值: {}", optimalK);

        // 步骤3: 计算数值特征归一化参数
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

        // 步骤4: 生成特征向量
        int dimension = clusterProperties.getDefaultDimension();
        List<DoublePoint> points = new ArrayList<>();
        Map<Integer, CommodityClean> pointToData = new HashMap<>();

        for (int i = 0; i < dataList.size(); i++) {
            CommodityClean c = dataList.get(i);
            double[] vector = generateCommodityFeatureVector(c, dimension, maxPrice, maxSales, maxReviewCount);
            DoublePoint point = new DoublePoint(vector);
            points.add(point);
            pointToData.put(points.indexOf(point), c);
        }

        // 步骤5: 执行KMeans++聚类
        List<CentroidCluster<DoublePoint>> clusterList = performClustering(points, optimalK);

        // 步骤6: 计算评估指标
        double sse = calculateSSE(points, clusterList);
        double silhouette = calculateSilhouette(points, clusterList);

        log.info("商品聚类完成: K={}, SSE={}, 轮廓系数={}", optimalK, sse, silhouette);

        // 步骤7: 保存聚类结果
        List<CommodityCluster> clusterResults = new ArrayList<>();
        List<ClusterInfo> clusterInfos = new ArrayList<>();

        for (int i = 0; i < clusterList.size(); i++) {
            CentroidCluster<DoublePoint> cluster = clusterList.get(i);
            List<DoublePoint> clusterPoints = cluster.getPoints();
            double[] center = cluster.getCenter().getPoint();

            ClusterInfo info = new ClusterInfo();
            info.setClusterId(i + 1);
            info.setDataType("commodity");
            info.setCenter(arrayToString(center));
            info.setSize(clusterPoints.size());
            info.setLabel("商品类别" + (i + 1));
            info.setCreateTime(new Date());
            clusterInfos.add(info);

            for (DoublePoint p : clusterPoints) {
                CommodityClean c = pointToData.get(points.indexOf(p));
                if (c != null) {
                    CommodityCluster cc = new CommodityCluster();
                    cc.setItemId(c.getItemId());
                    cc.setTitle(c.getTitle());
                    cc.setKeyword(c.getKeyword());
                    cc.setPrice(c.getPrice());
                    cc.setSales(c.getSales());
                    cc.setSource(c.getSource());
                    cc.setClusterId(i + 1);
                    cc.setClusterTime(new Date());
                    clusterResults.add(cc);
                }
            }
        }

        // 步骤8: 清空并写入commodity_cluster
        commodityClusterMapper.delete(null);
        clusterInfoMapper.delete(null);
        for (CommodityCluster cc : clusterResults) {
            commodityClusterMapper.insert(cc);
        }
        for (ClusterInfo info : clusterInfos) {
            clusterInfoMapper.insert(info);
        }

        result.put("success", true);
        result.put("message", "商品聚类完成");
        result.put("dataType", "commodity");
        result.put("optimalK", optimalK);
        result.put("sse", sse);
        result.put("silhouetteScore", silhouette);
        result.put("totalCount", dataList.size());
        result.put("clusterCount", optimalK);
        result.put("clusters", clusterInfos.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("clusterId", c.getClusterId());
            m.put("size", c.getSize());
            m.put("label", c.getLabel());
            return m;
        }).toList());

        return result;
    }

    /**
     * 生成热词特征向量
     */
    private double[] generateHotWordFeatureVector(HotWordClean hw, int dimension,
            double maxHeat, double maxSearchCount, double maxRelatedCount) {
        double[] vector = new double[dimension];

        if (hw.getKeyword() != null && !hw.getKeyword().isEmpty()) {
            char[] chars = hw.getKeyword().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                // 使用更复杂的哈希函数来生成索引
                int index = Math.abs((chars[i] * 31 + (i + 1) * 17) % dimension);
                // 增加字符的权重差异
                double value = (i + 1) * 0.1 + 1.0 / (1.0 + Math.abs(chars[i] - '中'));
                vector[index] += value;
            }
        }

        int textPart = dimension * 2 / 3;
        int idx = textPart;

        // 增加数值特征的权重
        if (hw.getHeat() != null && idx < dimension) {
            vector[idx++] = (hw.getHeat() / maxHeat) * 2.0;
        }
        if (hw.getSearchCount() != null && idx < dimension) {
            vector[idx++] = (hw.getSearchCount() / maxSearchCount) * 1.5;
        }
        if (hw.getRelatedCount() != null && idx < dimension) {
            vector[idx++] = (hw.getRelatedCount() / maxRelatedCount) * 1.0;
        }

        // 确保向量不为全0
        boolean allZero = true;
        for (double v : vector) {
            if (v != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            // 如果向量全0，添加一些随机值
            for (int i = 0; i < dimension; i++) {
                vector[i] = Math.random() * 0.1;
            }
        }

        return normalize(vector);
    }

    /**
     * 生成商品特征向量
     */
    private double[] generateCommodityFeatureVector(CommodityClean c, int dimension,
            double maxPrice, double maxSales, double maxReviewCount) {
        double[] vector = new double[dimension];

        if (c.getTitle() != null && !c.getTitle().isEmpty()) {
            char[] chars = c.getTitle().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                // 使用更复杂的哈希函数来生成索引
                int index = Math.abs((chars[i] * 31 + (i + 1) * 17) % dimension);
                // 增加字符的权重差异
                double value = (i + 1) * 0.1 + 1.0 / (1.0 + Math.abs(chars[i] - '中'));
                vector[index] += value;
            }
        }

        int textPart = dimension * 2 / 3;
        int idx = textPart;

        // 增加数值特征的权重
        if (c.getPrice() != null && idx < dimension) {
            vector[idx++] = (c.getPrice().doubleValue() / maxPrice) * 1.5;
        }
        if (c.getSales() != null && idx < dimension) {
            vector[idx++] = (c.getSales() / maxSales) * 2.0;
        }
        if (c.getReviewCount() != null && idx < dimension) {
            vector[idx++] = (c.getReviewCount() / maxReviewCount) * 1.0;
        }

        // 确保向量不为全0
        boolean allZero = true;
        for (double v : vector) {
            if (v != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            // 如果向量全0，添加一些随机值
            for (int i = 0; i < dimension; i++) {
                vector[i] = Math.random() * 0.1;
            }
        }

        return normalize(vector);
    }

    /**
     * L2归一化
     */
    private double[] normalize(double[] vector) {
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        return vector;
    }

    /**
     * 计算SSE
     */
    private double calculateSSE(List<DoublePoint> points, List<CentroidCluster<DoublePoint>> clusters) {
        double sse = 0;
        for (CentroidCluster<DoublePoint> cluster : clusters) {
            double[] center = cluster.getCenter().getPoint();
            for (DoublePoint p : cluster.getPoints()) {
                sse += computeDistanceSquared(p.getPoint(), center);
            }
        }
        return sse;
    }

    /**
     * 计算轮廓系数
     */
    private double calculateSilhouette(List<DoublePoint> points, List<CentroidCluster<DoublePoint>> clusters) {
        int n = points.size();
        if (n < 2) return 0;

        int[] assignments = new int[n];
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

        for (int i = 0; i < n; i++) {
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
            if (!Arrays.equals(p.getPoint(), point)) {
                totalDist += computeDistance(point, p.getPoint());
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
                double dist = computeDistance(point, clusters.get(i).getCenter().getPoint());
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }

        return minDist == Double.MAX_VALUE ? 0 : minDist;
    }

    private double computeDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private double computeDistanceSquared(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    /**
     * 执行KMeans++聚类
     */
    private List<CentroidCluster<DoublePoint>> performClustering(List<DoublePoint> points, int k) {
        // 增加最大迭代次数确保收敛
        int maxIterations = Math.max(300, clusterProperties.getMaxIterations());
        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<DoublePoint>(
                k,
                maxIterations
        );
        
        // 多次聚类取最优结果
        List<CentroidCluster<DoublePoint>> bestClusters = null;
        double bestSSE = Double.MAX_VALUE;
        
        for (int i = 0; i < 3; i++) {
            List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);
            double sse = calculateSSE(points, clusters);
            
            // 检查聚类结果是否合理（每个簇至少有一个样本）
            boolean isReasonable = true;
            for (CentroidCluster<DoublePoint> cluster : clusters) {
                if (cluster.getPoints().isEmpty()) {
                    isReasonable = false;
                    break;
                }
            }
            
            if (isReasonable && sse < bestSSE) {
                bestSSE = sse;
                bestClusters = clusters;
            }
        }
        
        if (bestClusters == null) {
            // 如果多次聚类都失败，使用默认聚类
            bestClusters = clusterer.cluster(points);
        }
        
        return bestClusters;
    }

    private String arrayToString(double[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== 其他接口实现 ====================

    @Override
    public List<Map<String, Object>> getClusteringResult(String dataType) {
        if (dataType == null || dataType.isEmpty()) {
            throw new IllegalArgumentException("数据类型不能为空");
        }

        if ("hotword".equals(dataType)) {
            return getHotWordClusterResult();
        } else if ("commodity".equals(dataType)) {
            return getCommodityClusterResult();
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + dataType);
        }
    }

    private List<Map<String, Object>> getHotWordClusterResult() {
        List<HotwordCluster> clusters = hotwordClusterMapper.selectList(null);
        List<HotWordClean> hotwords = hotWordCleanMapper.selectList(null);
        Map<Long, HotWordClean> hotwordMap = new HashMap<>();
        for (HotWordClean hw : hotwords) {
            hotwordMap.put(hw.getId(), hw);
        }

        return clusters.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("hotwordId", c.getHotwordId());
            HotWordClean hw = hotwordMap.get(c.getHotwordId());
            if (hw != null) {
                m.put("keyword", hw.getKeyword());
                m.put("heat", hw.getHeat());
                m.put("source", hw.getSource());
                m.put("searchCount", hw.getSearchCount());
                m.put("relatedCount", hw.getRelatedCount());
                m.put("hotScore", calculateHotWordScore(hw));
            }
            m.put("clusterId", c.getClusterId());
            m.put("distanceToCenter", c.getDistanceToCenter());
            return m;
        }).toList();
    }

    private List<Map<String, Object>> getCommodityClusterResult() {
        List<CommodityCluster> clusters = commodityClusterMapper.selectList(null);
        return clusters.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("itemId", c.getItemId());
            m.put("title", c.getTitle());
            m.put("keyword", c.getKeyword());
            m.put("price", c.getPrice());
            m.put("sales", c.getSales());
            m.put("source", c.getSource());
            m.put("clusterId", c.getClusterId());
            m.put("commodityScore", calculateCommodityScore(c));
            return m;
        }).toList();
    }

    /**
     * 计算热词热度评分
     * score = heat*0.5 + searchCount*0.3 + relatedCount*0.2
     */
    private double calculateHotWordScore(HotWordClean hw) {
        double heat = hw.getHeat() != null ? hw.getHeat() : 0;
        double searchCount = hw.getSearchCount() != null ? hw.getSearchCount() : 0;
        double relatedCount = hw.getRelatedCount() != null ? hw.getRelatedCount() : 0;
        return heat * 0.5 + searchCount * 0.3 + relatedCount * 0.2;
    }

    /**
     * 计算商品综合热度得分
     * score = sales*0.4 + reviewCount*0.3 + (1/price)*0.1
     */
    private double calculateCommodityScore(CommodityCluster c) {
        double sales = c.getSales() != null ? c.getSales() : 0;
        double price = c.getPrice() != null && c.getPrice().compareTo(BigDecimal.ZERO) > 0
                ? c.getPrice().doubleValue() : 1;
        return sales * 0.4 + 100 * 0.3 + (1.0 / price) * 0.1;
    }

    @Override
    public Map<String, Object> getClusteringChartData(String dataType) {
        if (dataType == null || dataType.isEmpty()) {
            throw new IllegalArgumentException("数据类型不能为空");
        }

        Map<String, Object> chartData = new HashMap<>();

        LambdaQueryWrapper<ClusterInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClusterInfo::getDataType, dataType);
        List<ClusterInfo> clusterInfos = clusterInfoMapper.selectList(wrapper);

        List<Map<String, Object>> pieData = clusterInfos.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", c.getLabel());
            m.put("value", c.getSize());
            return m;
        }).toList();

        List<Map<String, Object>> barData = clusterInfos.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("clusterId", c.getClusterId());
            m.put("label", c.getLabel());
            m.put("size", c.getSize());
            return m;
        }).toList();

        List<Map<String, Object>> keywordsByCluster = new ArrayList<>();
        for (ClusterInfo info : clusterInfos) {
            Map<String, Object> clusterMap = new HashMap<>();
            clusterMap.put("clusterId", info.getClusterId());
            clusterMap.put("label", info.getLabel());

            if ("hotword".equals(dataType)) {
                List<HotwordCluster> clusterHotwords = hotwordClusterMapper.selectList(null).stream()
                        .filter(c -> c.getClusterId().equals(info.getClusterId()))
                        .toList();
                List<HotWordClean> hotwords = hotWordCleanMapper.selectList(null);
                Map<Long, HotWordClean> hotwordMap = new HashMap<>();
                for (HotWordClean hw : hotwords) {
                    hotwordMap.put(hw.getId(), hw);
                }
                List<String> keywords = clusterHotwords.stream()
                        .map(c -> {
                            HotWordClean hw = hotwordMap.get(c.getHotwordId());
                            return hw != null ? hw.getKeyword() : "";
                        })
                        .filter(k -> !k.isEmpty())
                        .limit(20)
                        .toList();
                clusterMap.put("keywords", keywords);
            } else if ("commodity".equals(dataType)) {
                List<CommodityCluster> clusterCommodities = commodityClusterMapper.selectList(null).stream()
                        .filter(c -> c.getClusterId().equals(info.getClusterId()))
                        .toList();
                List<String> titles = clusterCommodities.stream()
                        .map(c -> c.getTitle() != null ? c.getTitle() : "")
                        .filter(t -> !t.isEmpty())
                        .limit(20)
                        .toList();
                clusterMap.put("keywords", titles);
            }

            keywordsByCluster.add(clusterMap);
        }

        chartData.put("pieData", pieData);
        chartData.put("barData", barData);
        chartData.put("keywordsByCluster", keywordsByCluster);
        chartData.put("totalCount", pieData.stream().mapToInt(m -> ((Number) m.get("value")).intValue()).sum());
        chartData.put("clusterCount", clusterInfos.size());

        return chartData;
    }

    @Override
    @Transactional
    public boolean clearClusteringData(String dataType) {
        if (dataType == null || dataType.isEmpty()) {
            throw new IllegalArgumentException("数据类型不能为空");
        }

        try {
            if ("hotword".equals(dataType)) {
                hotwordClusterMapper.delete(null);
            } else if ("commodity".equals(dataType)) {
                commodityClusterMapper.delete(null);
            }
            LambdaQueryWrapper<ClusterInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ClusterInfo::getDataType, dataType);
            clusterInfoMapper.delete(wrapper);
            return true;
        } catch (Exception e) {
            log.error("清空聚类数据失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getOptimalK(String dataType, int maxK) {
        if (dataType == null || dataType.isEmpty()) {
            throw new IllegalArgumentException("数据类型不能为空");
        }
        if (maxK <= 0) {
            maxK = 10;
        }

        List<HotWordClean> hotwordData = null;
        List<CommodityClean> commodityData = null;
        if ("hotword".equals(dataType)) {
            hotwordData = hotWordCleanMapper.selectList(null);
            if (hotwordData == null || hotwordData.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("optimalK", 3);
                result.put("message", "数据不足，返回默认K值3");
                return result;
            }
        } else if ("commodity".equals(dataType)) {
            commodityData = commodityCleanMapper.selectList(null);
            if (commodityData == null || commodityData.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("optimalK", 3);
                result.put("message", "数据不足，返回默认K值3");
                return result;
            }
        }

        List<Double> sseList = new ArrayList<>();
        List<Double> silhouetteList = new ArrayList<>();
        List<Integer> kValues = new ArrayList<>();

        int dimension = clusterProperties.getDefaultDimension();

        // 准备数据点
        List<DoublePoint> points = new ArrayList<>();
        if ("hotword".equals(dataType)) {
            // 计算热词特征的最大值
            double maxHeat = 1, maxSearchCount = 1, maxRelatedCount = 1;
            for (HotWordClean hw : hotwordData) {
                if (hw.getHeat() != null && hw.getHeat() > maxHeat) maxHeat = hw.getHeat();
                if (hw.getSearchCount() != null && hw.getSearchCount() > maxSearchCount) maxSearchCount = hw.getSearchCount();
                if (hw.getRelatedCount() != null && hw.getRelatedCount() > maxRelatedCount) maxRelatedCount = hw.getRelatedCount();
            }

            // 生成特征向量
            for (HotWordClean hw : hotwordData) {
                double[] vector = generateHotWordFeatureVector(hw, dimension, maxHeat, maxSearchCount, maxRelatedCount);
                points.add(new DoublePoint(vector));
            }
        } else if ("commodity".equals(dataType)) {
            // 计算商品特征的最大值
            double maxPrice = 1, maxSales = 1, maxReviewCount = 1;
            for (CommodityClean c : commodityData) {
                if (c.getPrice() != null && c.getPrice().doubleValue() > maxPrice) maxPrice = c.getPrice().doubleValue();
                if (c.getSales() != null && c.getSales() > maxSales) maxSales = c.getSales();
                if (c.getReviewCount() != null && c.getReviewCount() > maxReviewCount) maxReviewCount = c.getReviewCount();
            }

            // 生成特征向量
            for (CommodityClean c : commodityData) {
                double[] vector = generateCommodityFeatureVector(c, dimension, maxPrice, maxSales, maxReviewCount);
                points.add(new DoublePoint(vector));
            }
        }

        if (points.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("optimalK", 3);
            result.put("message", "无法生成特征向量，返回默认K值3");
            return result;
        }

        // 限制最大K值
        maxK = Math.min(maxK, Math.min(10, points.size() / 2));
        if (maxK < 2) {
            maxK = 2;
        }

        // 遍历不同的K值
        for (int k = 2; k <= maxK; k++) {
            KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(
                    k,
                    clusterProperties.getMaxIterations()
            );

            List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

            double sse = calculateSSE(points, clusters);
            double silhouette = calculateSilhouette(points, clusters);

            sseList.add(sse);
            silhouetteList.add(silhouette);
            kValues.add(k);

            log.info("K={}, SSE={}, 轮廓系数={}", k, sse, silhouette);
        }

        // 使用肘部法则和轮廓系数选择最佳K值
        int optimalK = findOptimalKUsingElbowMethod(kValues, sseList, silhouetteList);

        Map<String, Object> result = new HashMap<>();
        result.put("optimalK", optimalK);
        result.put("kValues", kValues);
        result.put("sseValues", sseList);
        result.put("silhouetteValues", silhouetteList);
        result.put("message", "成功计算最佳K值");

        return result;
    }

    private int findOptimalKUsingElbowMethod(List<Integer> kValues, List<Double> sseList, List<Double> silhouetteList) {
        if (kValues.isEmpty()) {
            return 3;
        }

        // 计算SSE的变化率
        List<Double> sseChanges = new ArrayList<>();
        for (int i = 1; i < sseList.size(); i++) {
            double change = sseList.get(i - 1) - sseList.get(i);
            sseChanges.add(change);
        }

        // 计算变化率的变化
        List<Double> sseChangeRates = new ArrayList<>();
        for (int i = 1; i < sseChanges.size(); i++) {
            double rate = sseChanges.get(i) / sseChanges.get(i - 1);
            sseChangeRates.add(rate);
        }

        // 找到肘部点
        int elbowIndex = 0;
        if (!sseChangeRates.isEmpty()) {
            double minRate = Double.MAX_VALUE;
            for (int i = 0; i < sseChangeRates.size(); i++) {
                if (sseChangeRates.get(i) < minRate) {
                    minRate = sseChangeRates.get(i);
                    elbowIndex = i + 2; // +2 because k starts from 2
                }
            }
        }

        // 如果肘部点不明显，使用轮廓系数最大值
        if (elbowIndex == 0 || elbowIndex >= kValues.size()) {
            double maxSilhouette = Double.MIN_VALUE;
            int maxSilhouetteIndex = 0;
            for (int i = 0; i < silhouetteList.size(); i++) {
                if (silhouetteList.get(i) > maxSilhouette) {
                    maxSilhouette = silhouetteList.get(i);
                    maxSilhouetteIndex = i;
                }
            }
            return kValues.get(maxSilhouetteIndex);
        }

        return kValues.get(elbowIndex);
    }

    @Override
    public double[] generateFeatureVector(String keyword, int dimension) {
        double[] vector = new double[dimension];
        if (keyword == null || keyword.isEmpty()) {
            return vector;
        }

        char[] chars = keyword.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int index = Math.abs((chars[i] * (i + 1)) % dimension);
            double value = 1.0 / (1.0 + Math.abs(chars[i] - '中'));
            vector[index] += value;
        }

        return normalize(vector);
    }
}
