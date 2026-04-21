package com.zcm.hotwordinsight.controller;

import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.service.VisualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据可视化控制器
 */
@RestController
@Tag(name = "数据可视化")
public class VisualController {

    @Resource
    private VisualService visualService;

    /**
     * 获取商品来源平台分布（饼图）
     */
    @Operation(summary = "获取商品来源平台分布")
    @CrossOrigin
    @GetMapping("/visual/commodity/source")
    public R<List<Map<String, Object>>> getCommoditySource() {
        List<Map<String, Object>> result = visualService.getCommoditySourceDistribution();
        return R.data(result);
    }

    /**
     * 获取商品销量排行（柱状图）
     */
    @Operation(summary = "获取商品销量排行")
    @CrossOrigin
    @GetMapping("/visual/commodity/sale")
    public R<List<Map<String, Object>>> getCommoditySaleRanking() {
        List<Map<String, Object>> result = visualService.getCommoditySaleRanking();
        return R.data(result);
    }

    /**
     * 获取商品价格区间统计（折线/柱状图）
     */
    @Operation(summary = "获取商品价格区间统计")
    @CrossOrigin
    @GetMapping("/visual/commodity/price")
    public R<List<Map<String, Object>>> getCommodityPriceDistribution() {
        List<Map<String, Object>> result = visualService.getCommodityPriceDistribution();
        return R.data(result);
    }

    /**
     * 获取商品类别占比（饼图）
     */
    @Operation(summary = "获取商品类别占比")
    @CrossOrigin
    @GetMapping("/visual/commodity/category")
    public R<List<Map<String, Object>>> getCommodityCategoryDistribution() {
        List<Map<String, Object>> result = visualService.getCommodityCategoryDistribution();
        return R.data(result);
    }

    /**
     * 获取热词来源平台分布（饼图）
     */
    @Operation(summary = "获取热词来源平台分布")
    @CrossOrigin
    @GetMapping("/visual/hotword/source")
    public R<List<Map<String, Object>>> getHotWordSource() {
        List<Map<String, Object>> result = visualService.getHotWordSourceDistribution();
        return R.data(result);
    }

    /**
     * 获取热词词频统计（词云图）
     */
    @Operation(summary = "获取热词词频统计")
    @CrossOrigin
    @GetMapping("/visual/hotword/frequency")
    public R<List<Map<String, Object>>> getHotWordFrequency() {
        List<Map<String, Object>> result = visualService.getHotWordFrequency();
        return R.data(result);
    }
}
