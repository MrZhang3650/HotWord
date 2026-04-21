package com.zcm.hotwordinsight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.service.ClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聚类分析控制器
 */
@Tag(name = "聚类分析管理")
@RestController
@RequestMapping("/cluster")
public class ClusterController extends BaseController {

    @Resource
    private ClusterService clusterService;

    @Operation(summary = "执行聚类分析")
    @PostMapping("/run")
    public R<Map<String, Object>> runClustering(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        int k = params.get("k") != null ? Integer.parseInt(params.get("k").toString()) : 3;
        Map<String, Object> result = clusterService.runClustering(dataType, k);
        return R.data(result);
    }

    @Operation(summary = "获取聚类结果列表")
    @PostMapping("/result")
    public R<List<Map<String, Object>>> getClusteringResult(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        List<Map<String, Object>> result = clusterService.getClusteringResult(dataType);
        return R.data(result);
    }

    @Operation(summary = "获取聚类图表数据")
    @PostMapping("/chart")
    public R<Map<String, Object>> getClusteringChartData(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        Map<String, Object> result = clusterService.getClusteringChartData(dataType);
        return R.data(result);
    }

    @Operation(summary = "清空历史聚类数据")
    @PostMapping("/clear")
    public R<Boolean> clearClusteringData(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        boolean result = clusterService.clearClusteringData(dataType);
        return R.data(result);
    }

    @Operation(summary = "获取最佳K值")
    @PostMapping("/optimalK")
    public R<Map<String, Object>> getOptimalK(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        int maxK = params.get("maxK") != null ? Integer.parseInt(params.get("maxK").toString()) : 10;
        Map<String, Object> result = clusterService.getOptimalK(dataType, maxK);
        return R.data(result);
    }
}
