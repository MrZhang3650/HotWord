package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcm.hotwordinsight.entity.HotWordClean;
import com.zcm.hotwordinsight.entity.RelationRule;
import com.zcm.hotwordinsight.mapper.RelationRuleMapper;
import com.zcm.hotwordinsight.service.HotWordCleanService;
import com.zcm.hotwordinsight.service.RelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关联分析服务实现类
 */
@Slf4j
@Service
public class RelationServiceImpl implements RelationService {

    @Resource
    private HotWordCleanService hotWordCleanService;

    @Resource
    private RelationRuleMapper relationRuleMapper;

    @Override
    @Transactional
    public Map<String, Object> runRelationAnalysis(String dataType, double minSupport, double minConfidence) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        // 参数校验
        if (minSupport < 0 || minSupport > 1) {
            result.put("success", false);
            result.put("message", "最小支持度必须在0-1之间");
            return result;
        }
        if (minConfidence < 0 || minConfidence > 1) {
            result.put("success", false);
            result.put("message", "最小置信度必须在0-1之间");
            return result;
        }

        List<HotWordClean> dataList = hotWordCleanService.list();
        if (dataList == null || dataList.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有可分析的数据");
            return result;
        }

        List<Set<String>> transactions = buildTransactions(dataList);
        if (transactions.isEmpty() || transactions.size() < 2) {
            result.put("success", false);
            result.put("message", "数据量不足，无法进行关联分析");
            return result;
        }

        int totalTransactions = transactions.size();
        log.info("开始关联分析，交易数: {}, 最小支持度: {}, 最小置信度: {}", totalTransactions, minSupport, minConfidence);

        Set<String> allItems = new HashSet<>();
        for (Set<String> transaction : transactions) {
            allItems.addAll(transaction);
        }
        log.info("唯一物品数: {}", allItems.size());

        Map<Set<String>, Integer> itemSetCounts = new HashMap<>();
        for (String item : allItems) {
            Set<String> itemSet = Collections.singleton(item);
            int count = 0;
            for (Set<String> transaction : transactions) {
                if (transaction.contains(item)) {
                    count++;
                }
            }
            itemSetCounts.put(itemSet, count);
        }

        // 存储所有频繁项集及其支持度
        Map<Set<String>, Double> allFrequentItemSets = new HashMap<>();
        List<Set<String>> frequentItemSets = new ArrayList<>();
        for (Map.Entry<Set<String>, Integer> entry : itemSetCounts.entrySet()) {
            double support = (double) entry.getValue() / totalTransactions;
            if (support >= minSupport) {
                frequentItemSets.add(entry.getKey());
                allFrequentItemSets.put(entry.getKey(), support);
            }
        }
        log.info("1-项频繁集数量: {}", frequentItemSets.size());

        int maxK = 3;
        for (int k = 2; k <= maxK; k++) {
            if (frequentItemSets.isEmpty()) {
                break;
            }

            List<Set<String>> candidates = generateCandidates(frequentItemSets, k);
            if (candidates.isEmpty()) {
                break;
            }
            log.info("第 {} 层候选集数量: {}", k, candidates.size());

            Map<Set<String>, Integer> candidateCounts = new HashMap<>();
            for (Set<String> candidate : candidates) {
                int count = 0;
                for (Set<String> transaction : transactions) {
                    if (transaction.containsAll(candidate)) {
                        count++;
                    }
                }
                candidateCounts.put(candidate, count);
            }

            List<Set<String>> newFrequentItemSets = new ArrayList<>();
            for (Map.Entry<Set<String>, Integer> entry : candidateCounts.entrySet()) {
                double support = (double) entry.getValue() / totalTransactions;
                if (support >= minSupport) {
                    newFrequentItemSets.add(entry.getKey());
                    allFrequentItemSets.put(entry.getKey(), support);
                }
            }
            log.info("第 {} 层频繁集数量: {}", k, newFrequentItemSets.size());

            if (newFrequentItemSets.isEmpty()) {
                break;
            }
            frequentItemSets = newFrequentItemSets;
        }

        List<RelationRule> rules = new ArrayList<>();
        for (Set<String> itemSet : frequentItemSets) {
            if (itemSet.size() < 2) {
                continue;
            }

            List<String> items = new ArrayList<>(itemSet);
            for (int mask = 1; mask < (1 << items.size()) - 1; mask++) {
                Set<String> antecedent = new HashSet<>();
                Set<String> consequent = new HashSet<>();

                for (int i = 0; i < items.size(); i++) {
                    if ((mask & (1 << i)) != 0) {
                        antecedent.add(items.get(i));
                    } else {
                        consequent.add(items.get(i));
                    }
                }

                if (!antecedent.isEmpty() && !consequent.isEmpty()) {
                    int antCount = 0;
                    int ruleCount = 0;
                    int consCount = 0;

                    for (Set<String> transaction : transactions) {
                        if (transaction.containsAll(antecedent)) {
                            antCount++;
                        }
                        if (transaction.containsAll(itemSet)) {
                            ruleCount++;
                        }
                        if (transaction.containsAll(consequent)) {
                            consCount++;
                        }
                    }

                    if (antCount > 0) {
                        double confidence = (double) ruleCount / antCount;
                        if (confidence >= minConfidence) {
                            double support = (double) ruleCount / totalTransactions;
                            double lift = consCount > 0 ? confidence / ((double) consCount / totalTransactions) : 0;

                            RelationRule rule = new RelationRule();
                            rule.setAntecedent(String.join(",", antecedent));
                            rule.setConsequent(String.join(",", consequent));
                            rule.setSupport(BigDecimal.valueOf(support).setScale(4, RoundingMode.HALF_UP));
                            rule.setConfidence(BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP));
                            rule.setLift(BigDecimal.valueOf(lift).setScale(4, RoundingMode.HALF_UP));
                            rule.setDataType(dataType);
                            rule.setCreateTime(new Date());
                            rules.add(rule);
                        }
                    }
                }
            }
        }

        relationRuleMapper.delete(null);
        for (RelationRule rule : rules) {
            relationRuleMapper.insert(rule);
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("关联分析完成，生成规则数: {}, 执行耗时: {}ms", rules.size(), executionTime);
        
        if (rules.isEmpty()) {
            result.put("success", false);
            result.put("message", "未挖掘到满足条件的关联规则，请降低支持度/置信度");
            return result;
        }

        // 计算统计指标
        double avgLift = rules.stream().mapToDouble(r -> r.getLift().doubleValue()).average().orElse(0);
        RelationRule strongestRule = rules.stream()
                .max(Comparator.comparing(RelationRule::getLift))
                .orElse(null);

        result.put("success", true);
        result.put("message", "关联分析完成");
        result.put("totalTransactions", totalTransactions);
        result.put("frequentItemSets", frequentItemSets.size());
        result.put("rulesCount", rules.size());
        result.put("executionTime", executionTime);
        result.put("avgLift", BigDecimal.valueOf(avgLift).setScale(4, RoundingMode.HALF_UP));
        
        if (strongestRule != null) {
            Map<String, Object> strongestRuleMap = new HashMap<>();
            strongestRuleMap.put("antecedent", strongestRule.getAntecedent());
            strongestRuleMap.put("consequent", strongestRule.getConsequent());
            strongestRuleMap.put("support", strongestRule.getSupport());
            strongestRuleMap.put("confidence", strongestRule.getConfidence());
            strongestRuleMap.put("lift", strongestRule.getLift());
            result.put("strongestRule", strongestRuleMap);
        }

        // 转换频繁项集为前端可展示的格式
        List<Map<String, Object>> frequentItemSetsList = allFrequentItemSets.entrySet().stream().map(entry -> {
            Map<String, Object> m = new HashMap<>();
            m.put("items", String.join(",", entry.getKey()));
            m.put("support", BigDecimal.valueOf(entry.getValue()).setScale(4, RoundingMode.HALF_UP));
            m.put("size", entry.getKey().size());
            return m;
        }).collect(Collectors.toList());

        result.put("frequentItemSetsList", frequentItemSetsList);
        result.put("rules", rules.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("antecedent", r.getAntecedent());
            m.put("consequent", r.getConsequent());
            m.put("support", r.getSupport());
            m.put("confidence", r.getConfidence());
            m.put("lift", r.getLift());
            // 添加关联强度标签
            double liftValue = r.getLift().doubleValue();
            if (liftValue > 1.5) {
                m.put("strength", "强关联");
            } else if (liftValue > 1) {
                m.put("strength", "弱关联");
            } else {
                m.put("strength", "无关联");
            }
            return m;
        }).collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<Map<String, Object>> getRelationList(String dataType) {
        LambdaQueryWrapper<RelationRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelationRule::getDataType, dataType);
        wrapper.orderByDesc(RelationRule::getConfidence);
        wrapper.orderByDesc(RelationRule::getSupport);

        List<RelationRule> rules = relationRuleMapper.selectList(wrapper);
        return rules.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("antecedent", r.getAntecedent());
            m.put("consequent", r.getConsequent());
            m.put("support", r.getSupport());
            m.put("confidence", r.getConfidence());
            m.put("lift", r.getLift());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getRelationChartData(String dataType) {
        Map<String, Object> chartData = new HashMap<>();

        LambdaQueryWrapper<RelationRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelationRule::getDataType, dataType);
        List<RelationRule> rules = relationRuleMapper.selectList(wrapper);

        Set<String> nodes = new HashSet<>();
        List<Map<String, Object>> links = new ArrayList<>();

        for (RelationRule rule : rules) {
            String[] antecedents = rule.getAntecedent().split(",");
            String[] consequents = rule.getConsequent().split(",");

            for (String a : antecedents) {
                nodes.add(a.trim());
            }
            for (String c : consequents) {
                nodes.add(c.trim());
            }

            for (String a : antecedents) {
                for (String c : consequents) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", a.trim());
                    link.put("target", c.trim());
                    link.put("confidence", rule.getConfidence());
                    link.put("support", rule.getSupport());
                    links.add(link);
                }
            }
        }

        List<Map<String, Object>> nodeList = new ArrayList<>();
        int index = 0;
        for (String node : nodes) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", index++);
            m.put("name", node);
            nodeList.add(m);
        }

        chartData.put("nodes", nodeList);
        chartData.put("links", links);
        chartData.put("totalRules", rules.size());

        return chartData;
    }

    @Override
    @Transactional
    public boolean clearRelationData(String dataType) {
        try {
            LambdaQueryWrapper<RelationRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RelationRule::getDataType, dataType);
            relationRuleMapper.delete(wrapper);
            return true;
        } catch (Exception e) {
            log.error("清空关联规则数据失败: {}", e.getMessage());
            return false;
        }
    }

    private List<Set<String>> buildTransactions(List<HotWordClean> dataList) {
        Map<String, Set<String>> sourceKeywordMap = new HashMap<>();
        for (HotWordClean data : dataList) {
            String source = data.getSource() != null ? data.getSource() : "unknown";
            if (!sourceKeywordMap.containsKey(source)) {
                sourceKeywordMap.put(source, new HashSet<>());
            }
            if (data.getKeyword() != null) {
                sourceKeywordMap.get(source).add(data.getKeyword());
            }
        }
        List<Set<String>> transactions = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : sourceKeywordMap.entrySet()) {
            if (entry.getValue().size() >= 2) {
                transactions.add(entry.getValue());
            }
        }
        return transactions;
    }

    private List<Set<String>> generateCandidates(List<Set<String>> frequentItemSets, int k) {
        if (frequentItemSets.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Set<String>> candidatesSet = new HashSet<>();
        List<Set<String>> list = new ArrayList<>(frequentItemSets);

        for (int i = 0; i < list.size(); i++) {
            Set<String> set1 = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                Set<String> set2 = list.get(j);
                Set<String> union = new HashSet<>(set1);
                union.addAll(set2);
                if (union.size() == k) {
                    candidatesSet.add(union);
                }
            }
        }

        return new ArrayList<>(candidatesSet);
    }
}
