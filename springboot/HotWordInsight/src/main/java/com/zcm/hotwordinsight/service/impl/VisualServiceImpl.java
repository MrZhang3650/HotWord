package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcm.hotwordinsight.entity.Commodity;
import com.zcm.hotwordinsight.entity.CommodityClean;
import com.zcm.hotwordinsight.entity.HotWord;
import com.zcm.hotwordinsight.entity.HotWordClean;
import com.zcm.hotwordinsight.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据可视化服务实现类
 */
@Slf4j
@Service
public class VisualServiceImpl implements VisualService {

    @Resource
    private CommodityService commodityService;

    @Resource
    private CommodityCleanService commodityCleanService;

    @Resource
    private HotWordService hotWordService;

    @Resource
    private HotWordCleanService hotWordCleanService;

    @Override
    public List<Map<String, Object>> getCommoditySourceDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<CommodityClean> commodityCleans = commodityCleanService.list();
            Map<String, Integer> sourceCount = new HashMap<>();
            
            for (CommodityClean commodity : commodityCleans) {
                String source = commodity.getSource() != null ? commodity.getSource() : "未知";
                sourceCount.put(source, sourceCount.getOrDefault(source, 0) + 1);
            }
            
            for (Map.Entry<String, Integer> entry : sourceCount.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取商品来源分布失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCommoditySaleRanking() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<CommodityClean> commodityCleans = commodityCleanService.list();
            List<Map<String, Object>> saleList = new ArrayList<>();
            
            for (CommodityClean commodity : commodityCleans) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", commodity.getTitle());
                item.put("sales", commodity.getSales());
                saleList.add(item);
            }
            
            saleList.sort((a, b) -> {
                Integer salesA = (Integer) a.get("sales");
                Integer salesB = (Integer) b.get("sales");
                return salesB.compareTo(salesA);
            });
            
            for (int i = 0; i < Math.min(10, saleList.size()); i++) {
                result.add(saleList.get(i));
            }
        } catch (Exception e) {
            log.error("获取商品销量排行失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCommodityPriceDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<CommodityClean> commodityCleans = commodityCleanService.list();
            Map<String, Integer> priceRangeCount = new HashMap<>();
            
            for (CommodityClean commodity : commodityCleans) {
                BigDecimal price = commodity.getPrice();
                if (price == null) continue;
                
                String range;
                if (price.compareTo(BigDecimal.valueOf(50)) < 0) {
                    range = "0-50元";
                } else if (price.compareTo(BigDecimal.valueOf(100)) < 0) {
                    range = "50-100元";
                } else if (price.compareTo(BigDecimal.valueOf(200)) < 0) {
                    range = "100-200元";
                } else if (price.compareTo(BigDecimal.valueOf(500)) < 0) {
                    range = "200-500元";
                } else {
                    range = "500元以上";
                }
                priceRangeCount.put(range, priceRangeCount.getOrDefault(range, 0) + 1);
            }
            
            List<String> ranges = List.of("0-50元", "50-100元", "100-200元", "200-500元", "500元以上");
            for (String range : ranges) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", range);
                item.put("value", priceRangeCount.getOrDefault(range, 0));
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取商品价格分布失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCommodityCategoryDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<CommodityClean> commodityCleans = commodityCleanService.list();
            Map<String, Integer> categoryCount = new HashMap<>();
            
            for (CommodityClean commodity : commodityCleans) {
                String category = commodity.getCategory() != null ? commodity.getCategory() : "其他";
                categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            }
            
            for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取商品类别分布失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getHotWordSourceDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<HotWordClean> hotWordCleans = hotWordCleanService.list();
            Map<String, Integer> sourceCount = new HashMap<>();
            
            for (HotWordClean hotWord : hotWordCleans) {
                String source = hotWord.getSource() != null ? hotWord.getSource() : "未知";
                sourceCount.put(source, sourceCount.getOrDefault(source, 0) + 1);
            }
            
            for (Map.Entry<String, Integer> entry : sourceCount.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取热词来源分布失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getHotWordFrequency() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<HotWordClean> hotWordCleans = hotWordCleanService.list();
            Map<String, Integer> frequencyMap = new HashMap<>();
            
            for (HotWordClean hotWord : hotWordCleans) {
                String keyword = hotWord.getKeyword();
                if (keyword != null) {
                    frequencyMap.put(keyword, frequencyMap.getOrDefault(keyword, 0) + 1);
                }
            }
            
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(frequencyMap.entrySet());
            sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            for (int i = 0; i < Math.min(50, sortedEntries.size()); i++) {
                Map.Entry<String, Integer> entry = sortedEntries.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("value", entry.getValue());
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取热词词频统计失败: {}", e.getMessage());
        }
        return result;
    }
}
