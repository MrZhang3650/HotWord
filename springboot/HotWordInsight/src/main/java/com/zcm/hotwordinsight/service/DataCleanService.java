package com.zcm.hotwordinsight.service;

import java.util.List;
import java.util.Map;

/**
 * 数据清洗服务接口
 */
public interface DataCleanService {

    /**
     * 清洗热词数据
     * 过滤条件：
     * - heat = 0 且 search_count = 0 的无效热词
     * - keyword 为空、长度 < 2 的垃圾词
     * - source 不规范数据
     * 去重规则：keyword + source 相同视为重复
     */
    Map<String, Object> cleanHotWordData();

    /**
     * 清洗商品数据
     * 过滤条件：
     * - price = 0 且 sales = 0 的无效商品
     * - source 不在 淘宝/京东/拼多多 范围内的数据
     * - item_id 重复数据（保留一条）
     */
    Map<String, Object> cleanCommodityData();

    /**
     * 获取清洗后的热词数据
     */
    List<Map<String, Object>> getCleanedHotWords();

    /**
     * 获取清洗后的商品数据
     */
    List<Map<String, Object>> getCleanedCommodities();
}
