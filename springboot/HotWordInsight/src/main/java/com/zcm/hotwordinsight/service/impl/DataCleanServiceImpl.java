package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcm.hotwordinsight.entity.CommodityClean;
import com.zcm.hotwordinsight.entity.HotWordClean;
import com.zcm.hotwordinsight.mapper.CommodityCleanMapper;
import com.zcm.hotwordinsight.mapper.HotWordCleanMapper;
import com.zcm.hotwordinsight.service.DataCleanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据清洗服务实现类
 */
@Slf4j
@Service
public class DataCleanServiceImpl implements DataCleanService {

    private static final Set<String> VALID_SOURCES = Set.of("淘宝", "京东", "拼多多", "淘宝天猫", "天猫");

    @Resource
    private HotWordCleanMapper hotWordCleanMapper;

    @Resource
    private CommodityCleanMapper commodityCleanMapper;

    private final List<Map<String, Object>> cleanedHotWords = new ArrayList<>();
    private final List<Map<String, Object>> cleanedCommodities = new ArrayList<>();

    @Override
    public Map<String, Object> cleanHotWordData() {
        Map<String, Object> result = new HashMap<>();
        int originalCount = 0;
        int filteredCount = 0;
        int duplicateCount = 0;

        try {
            List<HotWordClean> allData = hotWordCleanMapper.selectList(null);
            originalCount = allData.size();

            Set<String> seenKeys = new HashSet<>();
            List<HotWordClean> validData = new ArrayList<>();

            for (HotWordClean hw : allData) {
                String keyword = hw.getKeyword();
                String source = hw.getSource();

                if (keyword == null || keyword.trim().isEmpty() || keyword.length() < 2) {
                    filteredCount++;
                    continue;
                }

                if ((hw.getHeat() == null || hw.getHeat() == 0) &&
                    (hw.getSearchCount() == null || hw.getSearchCount() == 0)) {
                    filteredCount++;
                    continue;
                }

                String cleanSource = normalizeSource(source);
                if (cleanSource == null) {
                    filteredCount++;
                    continue;
                }

                String key = keyword.trim() + "_" + cleanSource;
                if (seenKeys.contains(key)) {
                    duplicateCount++;
                    continue;
                }
                seenKeys.add(key);

                hw.setSource(cleanSource);
                validData.add(hw);
            }

            hotWordCleanMapper.delete(null);

            cleanedHotWords.clear();
            for (HotWordClean hw : validData) {
                hotWordCleanMapper.insert(hw);

                Map<String, Object> map = new HashMap<>();
                map.put("id", hw.getId());
                map.put("keyword", hw.getKeyword());
                map.put("heat", hw.getHeat());
                map.put("searchCount", hw.getSearchCount());
                map.put("relatedCount", hw.getRelatedCount());
                map.put("source", hw.getSource());
                cleanedHotWords.add(map);
            }

            result.put("success", true);
            result.put("message", "热词数据清洗完成");
            result.put("originalCount", originalCount);
            result.put("filteredCount", filteredCount);
            result.put("duplicateCount", duplicateCount);
            result.put("validCount", validData.size());
            result.put("cleanedData", cleanedHotWords);

            log.info("热词数据清洗完成：原始{}条, 过滤{}条, 去重{}条, 有效{}条",
                    originalCount, filteredCount, duplicateCount, validData.size());

        } catch (Exception e) {
            log.error("热词数据清洗失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "热词数据清洗失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> cleanCommodityData() {
        Map<String, Object> result = new HashMap<>();
        int originalCount = 0;
        int filteredCount = 0;
        int duplicateCount = 0;

        try {
            List<CommodityClean> allData = commodityCleanMapper.selectList(null);
            originalCount = allData.size();

            Set<String> seenItemIds = new HashSet<>();
            List<CommodityClean> validData = new ArrayList<>();

            for (CommodityClean c : allData) {
                String itemId = c.getItemId();

                if (itemId == null || itemId.trim().isEmpty()) {
                    filteredCount++;
                    continue;
                }

                BigDecimal price = c.getPrice();
                Integer sales = c.getSales();
                if ((price == null || price.compareTo(BigDecimal.ZERO) == 0) &&
                    (sales == null || sales == 0)) {
                    filteredCount++;
                    continue;
                }

                String cleanSource = normalizeSource(c.getSource());
                if (cleanSource == null) {
                    filteredCount++;
                    continue;
                }

                if (seenItemIds.contains(itemId.trim())) {
                    duplicateCount++;
                    continue;
                }
                seenItemIds.add(itemId.trim());

                c.setSource(cleanSource);
                validData.add(c);
            }

            commodityCleanMapper.delete(null);

            cleanedCommodities.clear();
            for (CommodityClean c : validData) {
                commodityCleanMapper.insert(c);

                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("itemId", c.getItemId());
                map.put("title", c.getTitle());
                map.put("keyword", c.getKeyword());
                map.put("price", c.getPrice());
                map.put("sales", c.getSales());
                map.put("reviewCount", c.getReviewCount());
                map.put("source", c.getSource());
                cleanedCommodities.add(map);
            }

            result.put("success", true);
            result.put("message", "商品数据清洗完成");
            result.put("originalCount", originalCount);
            result.put("filteredCount", filteredCount);
            result.put("duplicateCount", duplicateCount);
            result.put("validCount", validData.size());
            result.put("cleanedData", cleanedCommodities);

            log.info("商品数据清洗完成：原始{}条, 过滤{}条, 去重{}条, 有效{}条",
                    originalCount, filteredCount, duplicateCount, validData.size());

        } catch (Exception e) {
            log.error("商品数据清洗失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "商品数据清洗失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getCleanedHotWords() {
        return new ArrayList<>(cleanedHotWords);
    }

    @Override
    public List<Map<String, Object>> getCleanedCommodities() {
        return new ArrayList<>(cleanedCommodities);
    }

    private String normalizeSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        source = source.trim();

        for (String valid : VALID_SOURCES) {
            if (source.contains(valid) || valid.contains(source)) {
                if (source.contains("天猫")) {
                    return "淘宝天猫";
                }
                return valid;
            }
        }

        if (source.contains("ebay") || source.contains("Amazon") ||
            source.contains("亚马逊") || source.contains("抖音") ||
            source.contains("小红书")) {
            return source;
        }

        return null;
    }
}
