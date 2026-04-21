package com.zcm.hotwordinsight.service;

import java.util.List;
import java.util.Map;

/**
 * 数据可视化服务接口
 */
public interface VisualService {

    /**
     * 获取商品来源平台分布
     */
    List<Map<String, Object>> getCommoditySourceDistribution();

    /**
     * 获取商品销量排行
     */
    List<Map<String, Object>> getCommoditySaleRanking();

    /**
     * 获取商品价格区间分布
     */
    List<Map<String, Object>> getCommodityPriceDistribution();

    /**
     * 获取商品类别占比
     */
    List<Map<String, Object>> getCommodityCategoryDistribution();

    /**
     * 获取热词来源平台分布
     */
    List<Map<String, Object>> getHotWordSourceDistribution();

    /**
     * 获取热词词频统计
     */
    List<Map<String, Object>> getHotWordFrequency();
}
